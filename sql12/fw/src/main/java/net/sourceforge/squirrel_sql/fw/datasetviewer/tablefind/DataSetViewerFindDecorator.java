package net.sourceforge.squirrel_sql.fw.datasetviewer.tablefind;

import net.sourceforge.squirrel_sql.fw.datasetviewer.DataSetViewerTablePanel;
import net.sourceforge.squirrel_sql.fw.datasetviewer.IDataSetViewer;
import net.sourceforge.squirrel_sql.fw.util.IMessageHandler;

import javax.swing.*;
import java.awt.*;

public class DataSetViewerFindDecorator
{
   private IDataSetViewer _dataSetViewer;
   private boolean _putTableInScrollpane;
   private final JSplitPane _split;
   private boolean _findPanelOpen;

   private JScrollPane _scrollPane;
   private DataSetFindPanelController _dataSetFindPanelController;


   public DataSetViewerFindDecorator(IDataSetViewer dataSetViewer, IMessageHandler messageHandler)
   {
      this(dataSetViewer, true, messageHandler);
   }

   public DataSetViewerFindDecorator(IDataSetViewer dataSetViewer, boolean putTableInScrollpane, IMessageHandler messageHandler)
   {
      _dataSetViewer = dataSetViewer;
      _putTableInScrollpane = putTableInScrollpane;

      _split = new JSplitPane();
      _split.setDividerSize(0);
      _split.setOrientation(JSplitPane.VERTICAL_SPLIT);
      _split.setDividerLocation(0);

      DataSetFindPanelListener dataSetFindPanelListener = new DataSetFindPanelListener()
      {
         @Override
         public void hideFindPanel()
         {
            toggleShowFindPanel();
         }
      };
      _dataSetFindPanelController = new DataSetFindPanelController(messageHandler, dataSetFindPanelListener);

      _split.setLeftComponent(_dataSetFindPanelController.getPanel());

      if (_dataSetViewer instanceof DataSetViewerTablePanel)
      {
         _dataSetFindPanelController.setDataSetViewerTablePanel((DataSetViewerTablePanel) _dataSetViewer);
      }
      else
      {
         _split.setLeftComponent(new JPanel());
      }


      if (putTableInScrollpane)
      {
         _scrollPane = new JScrollPane();
         _scrollPane.setBorder(BorderFactory.createEmptyBorder());
         _scrollPane.setViewportView(_dataSetViewer.getComponent());
         _split.setRightComponent(_scrollPane);

      }
      else
      {
         _split.setRightComponent(_dataSetViewer.getComponent());
      }
   }


   public IDataSetViewer getDataSetViewer()
   {
      return _dataSetViewer;
   }

   public Component getComponent()
   {
      return _split;
   }

   public boolean toggleShowFindPanel()
   {
      if (false == _dataSetViewer instanceof DataSetViewerTablePanel)
      {
         return false;
      }


      _findPanelOpen = !_findPanelOpen;
      if (_findPanelOpen)
      {
         _split.setDividerLocation(_dataSetFindPanelController.getPanel().getPreferredSize().height);
      }
      else
      {
         _split.setDividerLocation(0);
         _dataSetFindPanelController.wasHidden();
      }

      return true;
   }

   public void replaceDataSetViewer(IDataSetViewer dataSetViewer)
   {
      _dataSetViewer = dataSetViewer;
      if (_putTableInScrollpane)
      {
         _scrollPane.setViewportView(dataSetViewer.getComponent());
         _scrollPane.setRowHeader(null);
      }
      else
      {
         _split.setRightComponent(dataSetViewer.getComponent());
      }

      if (_dataSetViewer instanceof DataSetViewerTablePanel)
      {
         _dataSetFindPanelController.setDataSetViewerTablePanel((DataSetViewerTablePanel) _dataSetViewer);
      }
      else
      {
         _dataSetFindPanelController.setDataSetViewerTablePanel(null);
      }
   }
}
