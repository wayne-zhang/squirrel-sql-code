package net.sourceforge.squirrel_sql.plugins.jedit;
/*
 * Copyright (C) 2001-2003 Colin Bell
 * colbell@users.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.gjt.sp.jedit.syntax.JEditTextArea;
import org.gjt.sp.jedit.syntax.SyntaxDocument;

import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;

import net.sourceforge.squirrel_sql.client.IApplication;
import net.sourceforge.squirrel_sql.client.session.BaseSQLEntryPanel;
import net.sourceforge.squirrel_sql.client.session.ISession;
import net.sourceforge.squirrel_sql.client.session.SessionTextEditPopupMenu;

public class JeditSQLEntryPanel extends BaseSQLEntryPanel
{
	/** Logger for this class. */
	private static final ILogger s_log = LoggerController.createLogger(JeditSQLEntryPanel.class);

	/** Application API. */
	private IApplication _app;

	/** Text component. */
	private MyTextArea _textArea;

	/** Popup menu for this component. */
	private SessionTextEditPopupMenu _textPopupMenu;

	/** Listener for displaying the popup menu. */
	private MouseListener _sqlEntryMouseListener = new MyMouseListener();

	JeditSQLEntryPanel(ISession session, JeditPlugin plugin,
						JeditPreferences prefs)
	{
		super();

		if (session == null)
		{
			throw new IllegalArgumentException("Null ISession passed");
		}

		if (plugin == null)
		{
			throw new IllegalArgumentException("Null JeditPlugin passed");
		}

		if (prefs == null)
		{
			throw new IllegalArgumentException("Null JeditPreferences passed");
		}

		_app = session.getApplication();

		_textArea = new MyTextArea(session.getSQLConnection(), prefs);
		_textPopupMenu = new SessionTextEditPopupMenu(session);
	}

	/**
	 * @see ISQLEntryPanel#gettextComponent()
	 */
	public JComponent getTextComponent()
	{
		return _textArea;
	}

	/**
	 * If the component returned by <TT>getTextComponent</TT> contains
	 * its own scroll bars return <TT>true</TT> other wise this component
	 * will be wrapped in the scroll pane when added to the SQL panel.
	 * 
	 * @return	<TT>true</TT> if text component already handles scrolling.
	 */
	public boolean getDoesTextComponentHaveScroller()
	{
		return false;
	}

	/**
	 * @see ISQLEntryPanel#getText()
	 */
	public String getText()
	{
		return _textArea.getText();
	}

	public void setFont(Font font)
	{
		_textArea.setFont(font);
	}

	/**
	 * @see ISQLEntryPanel#getSelectedText()
	 */
	public String getSelectedText()
	{
		return _textArea.getSelectedText();
	}

	/**
	 * Replace the contents of the SQL entry area with the passed
	 * SQL script without selecting it.
	 *
	 * @param	sqlScript	The script to be placed in the SQL entry area..
	 */
	public void setText(String text)
	{
		setText(text, false);
	}

	/**
	 * Replace the contents of the SQL entry area with the passed
	 * SQL script and specify whether to select it.
	 *
	 * @param	sqlScript	The script to be placed in the SQL entry area..
	 * @param 	select		If <TT>true</TT> then select the passed script
	 *						in the sql entry area.
	 */
	public void setText(String text, boolean select)
	{
		_textArea.setText(text);
		setSelectionEnd(_textArea.getDocument().getLength());
		setSelectionStart(0);
	}

	/**
	 * Append the passed SQL script to the SQL entry area but don't select
	 * it.
	 *
	 * @param    sqlScript    The script to be appended.
	 */
	public void appendText(String sqlScript)
	{
		appendText(sqlScript, false);
	}

	/**
	 * Append the passed SQL script to the SQL entry area and specify
	 * whether it should be selected.
	 *
	 * @param    sqlScript    The script to be appended.
	 * @param    select        If <TT>true</TT> then select the passed script
	 *                         in the sql entry area.
	 */
	public void appendText(String sqlScript, boolean select)
	{
		Document doc = _textArea.getDocument();

		try
		{
			if (!getText().endsWith("\n") && !sqlScript.startsWith("\n"))
			{
				doc.insertString(doc.getLength(), "\n", null);
			}

			int start = 0;

			if (select)
			{
				start = doc.getLength();
			}

			doc.insertString(doc.getLength(), sqlScript, null);

			if (select)
			{
				setSelectionEnd(doc.getLength());
				setSelectionStart(start);
				_textArea.setCaretPosition(start);
			}
		}
		catch (Exception ex)
		{
			s_log.error("Error appending text to text area", ex);
		}
	}

	/**
	 * @see ISQLEntryPanel#getCaretPosition()
	 */
	public int getCaretPosition()
	{
		return _textArea.getCaretPosition();
	}

	public void setCaretPosition(int value)
	{
		_textArea.setCaretPosition(value);
	}

	/**
	 * @see ISQLEntryPanel#setTabSize(int)
	 */
	public void setTabSize(int tabSize)
	{
		_textArea.getDocument().putProperty(PlainDocument.tabSizeAttribute,
												new Integer(tabSize));
	}

	/**
	 * @see ISQLEntryPanel#getSelectionStart()
	 */
	public int getSelectionStart()
	{
		return _textArea.getSelectionStart();
	}

	/**
	 * @see ISQLEntryPanel#setSelectionStart(int)
	 */
	public void setSelectionStart(int pos)
	{
		_textArea.setSelectionStart(pos);
	}

	/**
	 * @see ISQLEntryPanel#getSelectionEnd()
	 */
	public int getSelectionEnd()
	{
		return _textArea.getSelectionEnd();
	}

	/**
	 * @see ISQLEntryPanel#setSelectionEnd(int)
	 */
	public void setSelectionEnd(int pos)
	{
		_textArea.setSelectionEnd(pos);
	}

	/**
	 * Replace the currently selected text in the SQL entry area
	 * with the passed text.
	 * 
	 * @param	sqlScript	The script to be placed in the SQL entry area.
	 */
	public void replaceSelection(String sqlScript)
	{
		_textArea.replaceSelection(sqlScript);
	}

	/**
	 * @see ISQLEntryPanel#hasFocus()
	 */
	public boolean hasFocus()
	{
		return _textArea.hasFocus();
	}

	/**
	 * @see ISQLEntryPanel#requestFocus()
	 */
	public void requestFocus()
	{
		_textArea.requestFocus();
	}

	/**
	 * Add a hierarchical menu to the SQL Entry Area popup menu.
	 *
	 * @param	menu	The menu that will be added.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if <TT>null</TT> <TT>Menu</TT> passed.
	 */
	public void addToSQLEntryAreaMenu(JMenu menu)
	{
		if (menu == null)
		{
			throw new IllegalArgumentException("Menu == null");
		}

		_textPopupMenu.add(menu);
	}

	/**
	 * Add an <TT>Action</TT> to the SQL Entry Area popup menu.
	 *
	 * @param	action	The action to be added.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if <TT>null</TT> <TT>Action</TT> passed.
	 */
	public void addToSQLEntryAreaMenu(Action action)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Action == null");
		}

		_textPopupMenu.add(action);
	}

	/**
	 * @see ISQLEntryPanel#addMouseListener(MouseListener)
	 */
	public void addMouseListener(MouseListener lis)
	{
		_textArea.addMouseListener(lis);
	}

	/**
	 * @see ISQLEntryPanel#removeMouseListener(MouseListener)
	 */
	public void removeMouseListener(MouseListener lis)
	{
		_textArea.removeMouseListener(lis);
	}

	void updateFromPreferences()
	{
		_textArea.updateFromPreferences();
	}

	/**
	 * @see ISQLEntryPanel#hasOwnUndoableManager()
	 */
	public boolean hasOwnUndoableManager()
	{
		return false;
	}

	/**
	 * @see ISQLEntryPanel#addUndoableEditListener(UndoableEditListener)
	 */
	public void addUndoableEditListener(UndoableEditListener listener)
	{
		_textArea.getDocument().addUndoableEditListener(listener);
	}

	/**
	 * @see ISQLEntryPanel#removeUndoableEditListener(UndoableEditListener)
	 */
	public void removeUndoableEditListener(UndoableEditListener listener)
	{
		_textArea.getDocument().removeUndoableEditListener(listener);
	}

	/**
	 * @see ISQLEntryPanel#setUndoActions(Action, Action)
	 */
	public void setUndoActions(Action undo, Action redo)
	{
		_textPopupMenu.addSeparator();
		_app.getResources().addToPopupMenu(undo, _textPopupMenu);
		_app.getResources().addToPopupMenu(redo, _textPopupMenu);
		_textPopupMenu.addSeparator();
	}

	/**
	 * @see ISQLEntryPanel#getCaretLineNumber()
	 */
	public int getCaretLineNumber()
	{
		return _textArea.getLineOfOffset(_textArea.getCaretPosition());
	}

	/**
	 * @see ISQLEntryPanel#getCaretLinePosition()
	 */
	public int getCaretLinePosition()
	{
		int caretPos = _textArea.getCaretPosition();
		int caretLineOffset = caretPos;
		caretLineOffset = _textArea.getLineStartOffset(getCaretLineNumber());

		return caretPos - caretLineOffset;
	}

	/**
	 * @see ISQLEntryPanel#addCaretListener(CaretListener)
	 */
	public void addCaretListener(CaretListener lis)
	{
		_textArea.addCaretListener(lis);
	}

	/**
	 * @see ISQLEntryPanel#removeCaretListener(CaretListener)
	 */
	public void removeCaretListener(CaretListener lis)
	{
		_textArea.removeCaretListener(lis);
	}

	private final class MyTextArea extends JEditTextArea
	{
		private final JeditPreferences _prefs;
		private final JeditTextAreaDefaults _taDfts;

		MyTextArea(SQLConnection conn, JeditPreferences prefs)
		{
			super();
			setEditorKit(new MoeSyntaxEditorKit(prefs));
			_prefs = prefs;
			_taDfts = new JeditTextAreaDefaults(this, prefs);

			final SyntaxDocument doc = getSyntaxDocument();
			doc.setTokenMarker(new JeditSQLTokenMarker(conn));
			doc.setTextAreaDefaults(_taDfts);

			updateFromPreferences();
		}

		void updateFromPreferences()
		{
			_taDfts.updateFromPreferences(_prefs);
			_taDfts.updateControl(this);
		}

		public void addNotify()
		{
			super.addNotify();
			JeditSQLEntryPanel.this.addMouseListener(_sqlEntryMouseListener);
		}

		public void removeNotify()
		{
			super.removeNotify();
			JeditSQLEntryPanel.this.removeMouseListener(_sqlEntryMouseListener);
		}
	}

	private final class MyMouseListener extends MouseAdapter
	{
		public void mousePressed(MouseEvent evt)
		{
			if (evt.isPopupTrigger())
			{
				displayPopupMenu(evt);
			}
		}

		public void mouseReleased(MouseEvent evt)
		{
			if (evt.isPopupTrigger())
			{
				displayPopupMenu(evt);
			}
		}

		private void displayPopupMenu(MouseEvent evt)
		{
			_textPopupMenu.setTextComponent(_textArea);
			_textPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}
}
