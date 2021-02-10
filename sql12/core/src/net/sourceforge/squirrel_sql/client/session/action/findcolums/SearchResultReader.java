package net.sourceforge.squirrel_sql.client.session.action.findcolums;

import net.sourceforge.squirrel_sql.client.session.ExtendedColumnInfo;
import net.sourceforge.squirrel_sql.client.session.schemainfo.SchemaInfo;
import net.sourceforge.squirrel_sql.fw.gui.GUIUtils;
import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;
import net.sourceforge.squirrel_sql.fw.util.StringManager;
import net.sourceforge.squirrel_sql.fw.util.StringManagerFactory;
import net.sourceforge.squirrel_sql.fw.util.Utilities;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SearchResultReader
{
   private static final StringManager s_stringMgr = StringManagerFactory.getStringManager(FindColumnsCtrl.class);

   private ExecutorService _executorService = Executors.newSingleThreadExecutor();
   private Object sync = new Object();
   private volatile boolean _doCancel = false;

   private FindColumnsDlg _dlg;
   private DislplayResultsCallback _dislplayResultsCallback;
   private Future<?> _currentFuture;

   public SearchResultReader(FindColumnsDlg dlg, DislplayResultsCallback dislplayResultsCallback)
   {
      _dlg = dlg;
      _dislplayResultsCallback = dislplayResultsCallback;
   }

   void cancel()
   {
      synchronized (sync)
      {
         if (_dlg.isCancelCloseOnCancel())
         {
            _doCancel = true;
         }
      }
   }

   void findAndShowResults(String filterString, SchemaInfo schemaInfo, final ArrayList<FindColumnsResultBean> searchResults, ITableInfo[] tableInfos)
   {
      try
      {
         _doCancel = true;
         if (null != _currentFuture)
         {
            // We don't want more than one thread running.
            _currentFuture.get();
         }

         _doCancel = false;
         SwingUtilities.invokeLater(() -> _findAndShowResults(0, filterString, schemaInfo, searchResults, tableInfos));
      }
      catch (Exception e)
      {
         throw Utilities.wrapRuntime(e);
      }
   }

   private void _findAndShowResults(int beginIndex, String filterString, SchemaInfo schemaInfo, final ArrayList<FindColumnsResultBean> searchResults, ITableInfo[] tableInfos)
   {
      try
      {
         long beginMillis = System.currentTimeMillis();

         for (int i = beginIndex; i < tableInfos.length; i++)
         {
            if(0 < i && System.currentTimeMillis() - beginMillis > 500)
            {
               GUIUtils.processOnSwingEventThread(() -> _dislplayResultsCallback.displayResult(searchResults));

               if(0 == beginIndex)
               {
                  System.out.println("#### Starting thread");
                  GUIUtils.processOnSwingEventThread(() -> _dlg.switchBtnCancelCloseToCancel());

                  int finalBeginIndex = i;
                  _currentFuture =
                        _executorService.submit(() -> _findAndShowResults(finalBeginIndex, filterString, schemaInfo, searchResults, tableInfos));
                  return;
               }
               else
               {
                  beginMillis = System.currentTimeMillis();
               }
            }

            ITableInfo tableInfo = tableInfos[i];

            final ExtendedColumnInfo[] columnInfos
                  = schemaInfo.getExtendedColumnInfos(tableInfo.getCatalogName(), tableInfo.getSchemaName(), tableInfo.getSimpleName());

            for (ExtendedColumnInfo columnInfo : columnInfos)
            {
               if(_doCancel)
               {
                  synchronized (sync)
                  {
                     System.out.println("#### Exit thread on cancel");
                     GUIUtils.processOnSwingEventThread(() -> cancelReadResultLoop());
                     return;
                  }
               }

               if(-1 < columnInfo.getColumnName().toLowerCase().indexOf(filterString))
               {
                  searchResults.add(createResultBean(tableInfo, columnInfo));
               }
            }
         }
         GUIUtils.processOnSwingEventThread(() -> finishReadResultLoop(searchResults));
      }
      catch(Throwable e)
      {
         GUIUtils.processOnSwingEventThread(() -> {throw Utilities.wrapRuntime(e);});
         // If we are in the thread, although the Throwable is already propagated to SQuirreL's UI,
         // we still want to finish the thread with an exception
         throw Utilities.wrapRuntime(e);
      }
   }

   private void cancelReadResultLoop()
   {
      _dlg.switchBtnCancelCloseToClose();
      _dlg.txtStatus.setText(_dlg.txtStatus.getText() + "  (" + s_stringMgr.getString("SearchResultReader.canceled") + ")");
   }

   private void finishReadResultLoop(ArrayList<FindColumnsResultBean> searchResults)
   {
      _dislplayResultsCallback.displayResult(searchResults);
      _dlg.switchBtnCancelCloseToClose();
   }

   private FindColumnsResultBean createResultBean(ITableInfo tableInfo, ExtendedColumnInfo columnInfo)
   {

      // Nice to test threading
      // try
      // {
      //    Thread.sleep(50);
      // }
      // catch (InterruptedException e)
      // {
      //    e.printStackTrace();
      // }

      final FindColumnsResultBean bean = new FindColumnsResultBean();
      bean.setCatalogName(tableInfo.getCatalogName());
      bean.setSchemaName(tableInfo.getSchemaName());
      bean.setObjectName(tableInfo.getSimpleName());
      bean.setObjectTypeName(tableInfo.getType());
      bean.setColumnName(columnInfo.getColumnName());
      bean.setColumnTypeName(columnInfo.getColumnType());
      bean.setNullable(columnInfo.isNullable() ? 1:0);
      bean.setSize(columnInfo.getColumnSize());
      bean.setPrecision(columnInfo.getTableColumnInfo().getRadix());
      bean.setDecimalDigits(columnInfo.getDecimalDigits());
      bean.setOrdinalPosition(columnInfo.getTableColumnInfo().getOrdinalPosition());
      bean.setRemarks(columnInfo.getRemarks());
      bean.setJavaSqlType(columnInfo.getColumnTypeID());
      return bean;
   }
}
