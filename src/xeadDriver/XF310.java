package xeadDriver;

/*
 * Copyright (c) 2011 WATANABE kozo <qyf05466@nifty.com>,
 * All rights reserved.
 *
 * This file is part of XEAD Driver.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the XEAD Project nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.text.DecimalFormat;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.w3c.dom.*;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class XF310 extends JDialog implements XFExecutable, XFScriptable {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element functionElement_ = null;
	private Session session_ = null;
	private Connection connection = null;
	private boolean instanceIsAvailable = true;
	private boolean isToBeCanceled = false;
	private boolean anyRecordsDeleted = false;
	private int instanceArrayIndex_ = -1;
	private int programSequence;
	private StringBuffer processLog = new StringBuffer();
	private XF310_KeyInputDialog keyInputDialog = null;
	private XF310_AddRowList addRowListDialog = null;
	private JPanel jPanelMain = new JPanel();
	private JPanel jPanelHeaderFields = new JPanel();
	private JScrollPane jScrollPaneHeaderFields = new JScrollPane();
	private XF310_HeaderTable headerTable;
	private XF310_HeaderField firstEditableHeaderField = null;
	private HashMap<String, Object> parmMap_ = null;
	private HashMap<String, Object> returnMap_ = new HashMap<String, Object>();
	private ArrayList<XF310_HeaderField> headerFieldList = new ArrayList<XF310_HeaderField>();
	private ArrayList<XF310_HeaderReferTable> headerReferTableList = new ArrayList<XF310_HeaderReferTable>();
	private NodeList headerReferElementList;
	private XF310_DetailTable detailTable;
	private ArrayList<XF310_DetailColumn> detailColumnList = new ArrayList<XF310_DetailColumn>();
	private ArrayList<XF310_DetailReferTable> detailReferTableList = new ArrayList<XF310_DetailReferTable>();
	private ArrayList<XF310_DetailRowNumber> deleteRowNumberList = new ArrayList<XF310_DetailRowNumber>();
	private ArrayList<WorkingRow> tableRowList = new ArrayList<WorkingRow>();
	private TableModelEditableList tableModelMain = null;
	private JTable jTableMain = new JTable();
	private String initialMsg = "";
	private NodeList detailReferElementList = null;
	private DefaultTableCellRenderer rendererTableHeader = null;
	private DefaultTableCellRenderer rendererAlignmentCenter = new DefaultTableCellRenderer();
	private DefaultTableCellRenderer rendererAlignmentRight = new DefaultTableCellRenderer();
	private DefaultTableCellRenderer rendererAlignmentLeft = new DefaultTableCellRenderer();
	private JPanel jPanelBottom = new JPanel();
	private JPanel jPanelButtons = new JPanel();
	private Action checkAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			messageList.clear();
			checkErrorsToUpdate(true);
			setMessagesOnPanel();
		}
	};
	private String buttonUpdateCaption = "";
	private JPanel jPanelInfo = new JPanel();
	private GridLayout gridLayoutButtons = new GridLayout();
	private GridLayout gridLayoutInfo = new GridLayout();
	private ArrayList<String> messageList = new ArrayList<String>();
	private JLabel jLabelFunctionID = new JLabel();
	private JLabel jLabelSessionID = new JLabel();
	private JSplitPane jSplitPaneMain = new JSplitPane();
	private JPanel jPanelCenter = new JPanel();
	private JScrollPane jScrollPaneTable = new JScrollPane();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private JTextArea jTextAreaMessages = new JTextArea();
	private Color selectionColorWithFocus = new Color(49,106,197);
	private Color selectionColorWithoutFocus = new Color(213,213,213);
	private SortableDomElementListModel sortingList1;
	private SortableDomElementListModel sortingList2;
	private Action helpAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			session_.browseHelp();
		}
	};
	private JPanel[] jPanelButtonArray = new JPanel[7];
	private JButton[] jButtonArray = new JButton[7];
	private Action[] actionButtonArray = new Action[7];
	private String[] actionDefinitionArray = new String[7];
	private ScriptEngine scriptEngine;
	private Bindings engineScriptBindings;
	private String scriptNameRunning = "";
	private final int FIELD_HORIZONTAL_MARGIN = 1;
	private final int FIELD_VERTICAL_MARGIN = 5;
	private final int FONT_SIZE = 14;
	private final int ROW_HEIGHT = 18;
	private ByteArrayOutputStream exceptionLog;
	private PrintStream exceptionStream;
	private String exceptionHeader = "";
	private boolean lastFocusedWasDetailColumn = false;
	private int detailRowNoLastValue = 0;
	private boolean isAbleToSetupKeysOfNewRecord;
	private String addRowListTitle = "";
	private KeyStroke keyStrokeToUpdate = null;
	private XFTableCellEditor activeCellEditor = null;

	public XF310(Session session, int instanceArrayIndex) {
		super(session, "", true);
		try {
			session_ = session;
			instanceArrayIndex_ = instanceArrayIndex;
			initComponentsAndVariants();
			//
		} catch(Exception e) {
			e.printStackTrace(exceptionStream);
		}
	}

	void initComponentsAndVariants() throws Exception {
		//
		jPanelMain.setLayout(new BorderLayout());
		jSplitPaneMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPaneMain.add(jPanelCenter, JSplitPane.TOP);
		jSplitPaneMain.add(jScrollPaneMessages, JSplitPane.BOTTOM);
		jSplitPaneMain.setFocusable(false);
		jScrollPaneHeaderFields.getViewport().add(jPanelHeaderFields, null);
		jScrollPaneHeaderFields.setBorder(null);
		jScrollPaneHeaderFields.setFocusable(false);
		jPanelHeaderFields.setLayout(null);
		jPanelHeaderFields.setFocusable(false);
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jPanelCenter.setLayout(new BorderLayout());
		jPanelCenter.add(jScrollPaneHeaderFields, BorderLayout.NORTH);
		jPanelCenter.add(jScrollPaneTable, BorderLayout.CENTER);
		//
		rendererAlignmentCenter.setHorizontalAlignment(SwingConstants.CENTER);
		rendererAlignmentRight.setHorizontalAlignment(SwingConstants.RIGHT);
		rendererAlignmentLeft.setHorizontalAlignment(SwingConstants.LEFT);
		rendererAlignmentCenter.setBackground(SystemColor.control);
		rendererAlignmentRight.setBackground(SystemColor.control);
		rendererAlignmentLeft.setBackground(SystemColor.control);
		jTableMain.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTableMain.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jTableMain.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableMain.setRowHeight(ROW_HEIGHT);
		jTableMain.setRowSelectionAllowed(true);
		jTableMain.addFocusListener(new XF310_jTableMain_focusAdapter(this));
		jTableMain.setAutoCreateRowSorter(true);
		jTableMain.getTableHeader().setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		InputMap im = jTableMain.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		final Action oldTabAction = jTableMain.getActionMap().get(im.get(tab));
		Action tabAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				oldTabAction.actionPerformed( e );
				//
				JTable table = (JTable)e.getSource();
				int rowCount = table.getRowCount();
				int columnCount = table.getColumnCount();
				int row = table.getSelectedRow();
				int column = table.getSelectedColumn();
				//
				if (row == 0 && column == 0 && lastFocusedWasDetailColumn) {
					lastFocusedWasDetailColumn = false;
					if (firstEditableHeaderField != null) {
						firstEditableHeaderField.requestFocus();
						return;
					}
				}
				//
				while (!table.getModel().isCellEditable(row, column)) {
					oldTabAction.actionPerformed( e );
					column += 1;
					if (column == columnCount) {
						column = 0;
						row +=1;
					}
					if (row == rowCount) {
						row = 0;
					}
					if (row == table.getSelectedRow() && column == table.getSelectedColumn()) {
						break;
					}
				}
				if (table.getModel().isCellEditable(row, column)) {
					lastFocusedWasDetailColumn = true;
					table.editCellAt(row, column);
					table.getEditorComponent().requestFocus();
				}
			}
		};
		jTableMain.getActionMap().put(im.get(tab), tabAction);
		rendererTableHeader = (DefaultTableCellRenderer)jTableMain.getTableHeader().getDefaultRenderer();
		rendererTableHeader.setHorizontalAlignment(SwingConstants.LEFT);
		jScrollPaneTable.getViewport().add(jTableMain, null);
		jScrollPaneTable.addMouseListener(new XF310_jScrollPaneTable_mouseAdapter(this));
		jScrollPaneTable.getViewport().add(jTableMain, null);
		//
		jPanelBottom.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setLayout(new BorderLayout());
		jPanelBottom.setBorder(null);
		jLabelFunctionID.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelFunctionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelFunctionID.setForeground(Color.gray);
		jLabelFunctionID.setFocusable(false);
		jLabelSessionID.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelSessionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelSessionID.setForeground(Color.gray);
		jLabelSessionID.setFocusable(false);
		jPanelButtons.setBorder(null);
		jPanelButtons.setLayout(gridLayoutButtons);
		jPanelButtons.setFocusable(false);
		gridLayoutInfo.setColumns(1);
		gridLayoutInfo.setRows(2);
		gridLayoutInfo.setVgap(4);
		jPanelInfo.setLayout(gridLayoutInfo);
		jPanelInfo.add(jLabelSessionID);
		jPanelInfo.add(jLabelFunctionID);
		jPanelInfo.setFocusable(false);
		gridLayoutButtons.setColumns(7);
		gridLayoutButtons.setRows(1);
		gridLayoutButtons.setHgap(2);
		//
		for (int i = 0; i < 7; i++) {
			jButtonArray[i] = new JButton();
			jButtonArray[i].setBounds(new Rectangle(0, 0, 90, 30));
			jButtonArray[i].setFocusable(false);
			jButtonArray[i].addActionListener(new XF310_FunctionButton_actionAdapter(this));
			jPanelButtonArray[i] = new JPanel();
			jPanelButtonArray[i].setLayout(new BorderLayout());
			jPanelButtonArray[i].add(jButtonArray[i], BorderLayout.CENTER);
			actionButtonArray[i] = new ButtonAction(jButtonArray[i]);
			jPanelButtons.add(jPanelButtonArray[i]);
		}
		//
		jPanelMain.add(jSplitPaneMain, BorderLayout.CENTER);
		jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
	}

	public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parmMap) {
		org.w3c.dom.Element workElement;
		String workStr, workAlias, workTableID, workFieldID;
		StringTokenizer workTokenizer;

		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			////////////////////////
			// Process parameters //
			////////////////////////
			parmMap_ = parmMap;
			if (parmMap_ == null) {
				parmMap_ = new HashMap<String, Object>();
			}
			returnMap_.clear();
			returnMap_.putAll(parmMap_);
			returnMap_.put("RETURN_CODE", "21");

			///////////////////////////
			// Initializing variants //
			///////////////////////////
			instanceIsAvailable = false;
			isToBeCanceled = false;
			exceptionLog = new ByteArrayOutputStream();
			exceptionStream = new PrintStream(exceptionLog);
			exceptionHeader = "";
			processLog.delete(0, processLog.length());
			messageList.clear();
			functionElement_ = functionElement;
			connection = session_.getConnection();
			programSequence = session_.writeLogOfFunctionStarted(functionElement_.getAttribute("ID"), functionElement_.getAttribute("Name"));
			keyInputDialog = null;
			anyRecordsDeleted = false;

			//////////////////////////////////////
			// Setup Script Engine and Bindings //
			//////////////////////////////////////
			scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
			engineScriptBindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
			engineScriptBindings.clear();
			engineScriptBindings.put("instance", (XFScriptable)this);
			
			//////////////////////////////
			// Set panel configurations //
			//////////////////////////////
			jLabelSessionID.setText(session_.getSessionID());
			jLabelFunctionID.setText("310" + "-" + instanceArrayIndex_ + "-" + functionElement_.getAttribute("ID"));
			FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font("Dialog", 0, FONT_SIZE));
			jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));
			this.setTitle(functionElement_.getAttribute("Name"));
	        Rectangle screenRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			if (functionElement_.getAttribute("Size").equals("")) {
				this.setPreferredSize(new Dimension(screenRect.width, screenRect.height));
				this.setLocation(0, 0);
			} else {
				workTokenizer = new StringTokenizer(functionElement_.getAttribute("Size"), ";" );
				int width = Integer.parseInt(workTokenizer.nextToken());
				int height = Integer.parseInt(workTokenizer.nextToken());
				this.setPreferredSize(new Dimension(width, height));
				int posX = (screenRect.width - width) / 2;
				int posY = (screenRect.height - height) / 2;
				this.setLocation(posX, posY);
			}
			initialMsg = functionElement_.getAttribute("InitialMsg");

			/////////////////////////////////////////////////
			// Setup information of Header Table and Lists //
			/////////////////////////////////////////////////
			headerTable = new XF310_HeaderTable(functionElement_, this);
			//
			headerReferTableList.clear();
			headerReferElementList = headerTable.getTableElement().getElementsByTagName("Refer");
			sortingList1 = XFUtility.getSortedListModel(headerReferElementList, "Order");
			for (int i = 0; i < sortingList1.getSize(); i++) {
				headerReferTableList.add(new XF310_HeaderReferTable((org.w3c.dom.Element)sortingList1.getElementAt(i), this));
			}

			/////////////////////////////////////////////////
			// Setup Header Fields and Fetch Header Record //
			/////////////////////////////////////////////////
			jPanelHeaderFields.removeAll();
			headerFieldList.clear();
			firstEditableHeaderField = null;
			Dimension dimOfPriviousField = new Dimension(0,0);
			Dimension dim = new Dimension(0,0);
			int posX = 0;
			int posY = 0;
			int biggestWidth = 300;
			int biggestHeight = 30;
			boolean firstVisibleField = true;
			//
			// Add visible fields on the header field list //
			NodeList headerFieldElementList = functionElement_.getElementsByTagName("Field");
			sortingList1 = XFUtility.getSortedListModel(headerFieldElementList, "Order");
			for (int i = 0; i < sortingList1.getSize(); i++) {
				//
				headerFieldList.add(new XF310_HeaderField((org.w3c.dom.Element)sortingList1.getElementAt(i), this));
				if (firstVisibleField) {
					posX = 0;
					posY = this.FIELD_VERTICAL_MARGIN + 3;
					firstVisibleField = false;
				} else {
					if (headerFieldList.get(i).isHorizontal()) {
						posX = posX + dimOfPriviousField.width + headerFieldList.get(i).getPositionMargin() + this.FIELD_HORIZONTAL_MARGIN;
					} else {
						posX = 0;
						posY = posY + dimOfPriviousField.height+ headerFieldList.get(i).getPositionMargin() + this.FIELD_VERTICAL_MARGIN;
					}
				}
				dim = headerFieldList.get(i).getPreferredSize();
				headerFieldList.get(i).setBounds(posX, posY, dim.width, dim.height);
				jPanelHeaderFields.add(headerFieldList.get(i));
				//
				if (posX + dim.width > biggestWidth) {
					biggestWidth = posX + dim.width;
				}
				//
				if (posY + dim.height > biggestHeight) {
					biggestHeight = posY + dim.height;
				}
				//
				if (headerFieldList.get(i).isHorizontal()) {
					dimOfPriviousField = new Dimension(dim.width, XFUtility.FIELD_UNIT_HEIGHT);
				} else {
					dimOfPriviousField = new Dimension(dim.width, dim.height);
				}
			}
			//
			// Add header table keys as HIDDEN fields if they are not on the header field list //
			for (int i = 0; i < headerTable.getKeyFieldIDList().size(); i++) {
				if (!containsHeaderField(headerTable.getTableID(), "", headerTable.getKeyFieldIDList().get(i))) {
					headerFieldList.add(new XF310_HeaderField(headerTable.getTableID(), "", headerTable.getKeyFieldIDList().get(i), this));
				}
			}
			//
			// Add unique key fields as HIDDEN fields if they are not on the header field list //
			for (int i = 0; i < headerTable.getUniqueKeyList().size(); i++) {
				workTokenizer = new StringTokenizer(headerTable.getUniqueKeyList().get(i), ";" );
				while (workTokenizer.hasMoreTokens()) {
					workFieldID = workTokenizer.nextToken();
					if (!containsHeaderField(headerTable.getTableID(), "", workFieldID)) {
						headerFieldList.add(new XF310_HeaderField(headerTable.getTableID(), "", workFieldID, this));
					}
				}
			}
			//
			// Analyze fields in scripts and add them as HIDDEN columns if necessary //
			for (int i = 0; i < headerTable.getScriptList().size(); i++) {
				if	(headerTable.getScriptList().get(i).isToBeRunAtEvent("BR", "")
				|| headerTable.getScriptList().get(i).isToBeRunAtEvent("AR", "")
				|| headerTable.getScriptList().get(i).isToBeRunAtEvent("BU", "")
				|| headerTable.getScriptList().get(i).isToBeRunAtEvent("AU", "")) {
					for (int j = 0; j < headerTable.getScriptList().get(i).getFieldList().size(); j++) {
						workTokenizer = new StringTokenizer(headerTable.getScriptList().get(i).getFieldList().get(j), "." );
						workAlias = workTokenizer.nextToken();
						workTableID = getTableIDOfTableAlias(workAlias);
						workFieldID = workTokenizer.nextToken();
						if (!containsHeaderField(workTableID, workAlias, workFieldID)) {
							workElement = session_.getFieldElement(workTableID, workFieldID);
							if (workElement == null) {
								String msg = res.getString("FunctionError1") + headerTable.getTableID() + res.getString("FunctionError2") + headerTable.getScriptList().get(i).getName() + res.getString("FunctionError3") + workAlias + "_" + workFieldID + res.getString("FunctionError4");
								JOptionPane.showMessageDialog(null, msg);
								throw new Exception(msg);
							} else {
								if (headerTable.isValidDataSource(workTableID, workAlias, workFieldID)) {
									headerFieldList.add(new XF310_HeaderField(workTableID, workAlias, workFieldID, this));
								}
							}
						}
					}
				}
			}
			//
			// Analyze header refer tables and add their fields as HIDDEN header fields if necessary //
			for (int i = headerReferTableList.size()-1; i > -1; i--) {
				for (int j = 0; j < headerReferTableList.get(i).getFieldIDList().size(); j++) {
					if (containsHeaderField(headerReferTableList.get(i).getTableID(), headerReferTableList.get(i).getTableAlias(), headerReferTableList.get(i).getFieldIDList().get(j))) {
						headerReferTableList.get(i).setToBeExecuted(true);
						break;
					}
				}
				if (!headerReferTableList.get(i).isToBeExecuted()) {
					for (int j = 0; j < headerReferTableList.get(i).getWithKeyFieldIDList().size(); j++) {
						workTokenizer = new StringTokenizer(headerReferTableList.get(i).getWithKeyFieldIDList().get(j), "." );
						workAlias = workTokenizer.nextToken();
						workFieldID = workTokenizer.nextToken();
						if (workAlias.equals(headerTable.getTableID()) && containsHeaderField(headerTable.getTableID(), "", workFieldID)) {
							headerReferTableList.get(i).setToBeExecuted(true);
							break;
						}
					}
				}
				if (headerReferTableList.get(i).isToBeExecuted()) {
					for (int j = 0; j < headerReferTableList.get(i).getFieldIDList().size(); j++) {
						if (!containsHeaderField(headerReferTableList.get(i).getTableID(), headerReferTableList.get(i).getTableAlias(), headerReferTableList.get(i).getFieldIDList().get(j))) {
							headerFieldList.add(new XF310_HeaderField(headerReferTableList.get(i).getTableID(), headerReferTableList.get(i).getTableAlias(), headerReferTableList.get(i).getFieldIDList().get(j), this));
						}
					}
					for (int j = 0; j < headerReferTableList.get(i).getWithKeyFieldIDList().size(); j++) {
						workTokenizer = new StringTokenizer(headerReferTableList.get(i).getWithKeyFieldIDList().get(j), "." );
						workAlias = workTokenizer.nextToken();
						workTableID = getTableIDOfTableAlias(workAlias);
						workFieldID = workTokenizer.nextToken();
						if (!containsHeaderField(workTableID, workAlias, workFieldID)) {
							headerFieldList.add(new XF310_HeaderField(workTableID, workAlias, workFieldID, this));
						}
					}
				}
			}
			//
			// Add detail fields on header table as HIDDEN fields if they are not on the header fields list //
			NodeList columnFieldList = functionElement_.getElementsByTagName("Column");
			for (int i = 0; i < columnFieldList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)columnFieldList.item(i);
				workStr = workElement.getAttribute("DataSource");
				if (workStr.substring(0, workStr.indexOf(".")).equals(headerTable.getTableID())) {
					if (!containsHeaderField(headerTable.getTableID(), "", workStr.substring(workStr.indexOf(".") + 1, workStr.length()))) {
						headerFieldList.add(new XF310_HeaderField(headerTable.getTableID(), "", workStr.substring(workStr.indexOf(".") + 1, workStr.length()), this));
					}
				}
			}
//			//
//			// additional setup for comboBox //
//			for (int i = 0; i < headerFieldList.size(); i++) {
//				headerFieldList.get(i).setupComboBoxData();
//			}
			//
			jPanelHeaderFields.setPreferredSize(new Dimension(biggestWidth, biggestHeight));
			this.pack();
			jSplitPaneMain.setDividerLocation(this.getPreferredSize().height - 118);
			this.pack();

			/////////////////////////
			// Fetch Header Record //
			/////////////////////////
			fetchHeaderRecord();

			if (!this.isToBeCanceled) {

				///////////////////////////////////////
				// Setup information of detail table //
				///////////////////////////////////////
				detailTable = new XF310_DetailTable(functionElement_, this);
				detailReferTableList.clear();
				detailReferElementList = detailTable.getTableElement().getElementsByTagName("Refer");
				sortingList2 = XFUtility.getSortedListModel(detailReferElementList, "Order");
				for (int j = 0; j < sortingList2.getSize(); j++) {
					detailReferTableList.add(new XF310_DetailReferTable((org.w3c.dom.Element)sortingList2.getElementAt(j), this));
				}
				deleteRowNumberList.clear();

				///////////////////////////////////////////
				// Setup components for JTable of Detail //
				///////////////////////////////////////////
				tableModelMain = new TableModelEditableList();
				jTableMain.setModel(tableModelMain);
				TableColumn column = null;
				detailColumnList.clear();
				//
				// Add Column on JTable //
				tableModelMain.addColumn("NO."); //column index:0 //
				int columnIndex = 0;
				columnFieldList = functionElement_.getElementsByTagName("Column");
				sortingList2 = XFUtility.getSortedListModel(columnFieldList, "Order");
				for (int j = 0; j < sortingList2.getSize(); j++) {
					detailColumnList.add(new XF310_DetailColumn((org.w3c.dom.Element)sortingList2.getElementAt(j), this));
					if (detailColumnList.get(j).isVisibleOnPanel()) {
						columnIndex++;
						detailColumnList.get(j).setColumnIndex(columnIndex);
						tableModelMain.addColumn(detailColumnList.get(j).getCaption());
					}
				}
				//
				// Set attributes to each column //
				column = jTableMain.getColumnModel().getColumn(0);
				column.setPreferredWidth(38);
				column.setCellRenderer(new XF310_DetailRowNumberRenderer());
				for (int j = 0; j < detailColumnList.size(); j++) {
					if (detailColumnList.get(j).isVisibleOnPanel()) {
						column = jTableMain.getColumnModel().getColumn(detailColumnList.get(j).getColumnIndex());
						column.setPreferredWidth(detailColumnList.get(j).getWidth());
						column.setCellRenderer(detailColumnList.get(j).getCellRenderer());
						column.setCellEditor(detailColumnList.get(j).getCellEditor());
					}
				}
				//
				// Add detail table key fields as HIDDEN column if they are not on column list //
				for (int j = 0; j < detailTable.getKeyFieldIDList().size(); j++) {
					if (!containsDetailField(detailTable.getTableID(), "", detailTable.getKeyFieldIDList().get(j))) {
						detailColumnList.add(new XF310_DetailColumn(detailTable.getTableID(), "", detailTable.getKeyFieldIDList().get(j), this));
					}
				}
				//
				// Add unique key fields as HIDDEN column if they are not on the column list //
				for (int i = 0; i < detailTable.getUniqueKeyList().size(); i++) {
					workTokenizer = new StringTokenizer(detailTable.getUniqueKeyList().get(i), ";" );
					while (workTokenizer.hasMoreTokens()) {
						workFieldID = workTokenizer.nextToken();
						if (!containsDetailField(detailTable.getTableID(), "", workFieldID)) {
							detailColumnList.add(new XF310_DetailColumn(detailTable.getTableID(), "", workFieldID, this));
						}
					}
				}
				//
				// Add order-By fields as HIDDEN column if they are not on the column list //
				for (int j = 0; j < detailTable.getOrderByFieldIDList().size(); j++) {
					workStr = detailTable.getOrderByFieldIDList().get(j).replace("(D)", "");
					workStr = workStr.replace("(A)", "");
					workTokenizer = new StringTokenizer(workStr, "." );
					workAlias = workTokenizer.nextToken();
					workTableID = getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!containsDetailField(workTableID, workAlias, workFieldID)) {
						detailColumnList.add(new XF310_DetailColumn(workTableID, workAlias, workFieldID, this));
					}
				}
				//
				// Analyze fields in scripts and add them as HIDDEN columns if necessary //
				for (int j = 0; j < detailTable.getScriptList().size(); j++) {
					if	(detailTable.getScriptList().get(j).isToBeRunAtEvent("BC", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("AC", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("BR", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("AR", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("BU", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("AU", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("BD", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("AD", "")) {
						for (int k = 0; k < detailTable.getScriptList().get(j).getFieldList().size(); k++) {
							workTokenizer = new StringTokenizer(detailTable.getScriptList().get(j).getFieldList().get(k), "." );
							workAlias = workTokenizer.nextToken();
							workTableID = getTableIDOfTableAlias(workAlias);
							workFieldID = workTokenizer.nextToken();
							if (!containsDetailField(workTableID, workAlias, workFieldID)) {
								workElement = session_.getFieldElement(workTableID, workFieldID);
								if (workElement == null) {
									String msg = res.getString("FunctionError1") + detailTable.getTableID() + res.getString("FunctionError2") + detailTable.getScriptList().get(j).getName() + res.getString("FunctionError3") + workAlias + "_" + workFieldID + res.getString("FunctionError4");
									JOptionPane.showMessageDialog(null, msg);
									throw new Exception(msg);
								} else {
									if (detailTable.isValidDataSource(workTableID, workAlias, workFieldID)) {
										detailColumnList.add(new XF310_DetailColumn(workTableID, workAlias, workFieldID, this));
									}
								}
							}
						}
					}
				}
				//
				// Analyze detail refer tables and add their fields as HIDDEN columns if necessary //
				for (int j = detailReferTableList.size()-1; j > -1; j--) {
					for (int k = 0; k < detailReferTableList.get(j).getFieldIDList().size(); k++) {
						if (containsDetailField(detailReferTableList.get(j).getTableID(), detailReferTableList.get(j).getTableAlias(), detailReferTableList.get(j).getFieldIDList().get(k))) {
							detailReferTableList.get(j).setToBeExecuted(true);
							break;
						}
					}
					if (!detailReferTableList.get(j).isToBeExecuted()) {
						for (int k = 0; k < detailReferTableList.get(j).getWithKeyFieldIDList().size(); k++) {
							workTokenizer = new StringTokenizer(detailReferTableList.get(j).getWithKeyFieldIDList().get(k), "." );
							workAlias = workTokenizer.nextToken();
							workFieldID = workTokenizer.nextToken();
							if (workAlias.equals(detailTable.getTableID()) && containsDetailField(detailTable.getTableID(), "", workFieldID)) {
								detailReferTableList.get(j).setToBeExecuted(true);
								break;
							}
						}
					}
					if (detailReferTableList.get(j).isToBeExecuted()) {
						for (int k = 0; k < detailReferTableList.get(j).getFieldIDList().size(); k++) {
							if (!containsDetailField(detailReferTableList.get(j).getTableID(), detailReferTableList.get(j).getTableAlias(), detailReferTableList.get(j).getFieldIDList().get(k))) {
								detailColumnList.add(new XF310_DetailColumn(detailReferTableList.get(j).getTableID(), detailReferTableList.get(j).getTableAlias(), detailReferTableList.get(j).getFieldIDList().get(k), this));
							}
						}
						for (int k = 0; k < detailReferTableList.get(j).getWithKeyFieldIDList().size(); k++) {
							workTokenizer = new StringTokenizer(detailReferTableList.get(j).getWithKeyFieldIDList().get(k), "." );
							workAlias = workTokenizer.nextToken();
							workTableID = getTableIDOfTableAlias(workAlias);
							workFieldID = workTokenizer.nextToken();
							if (!containsDetailField(workTableID, workAlias, workFieldID)) {
								detailColumnList.add(new XF310_DetailColumn(workTableID, workAlias, workFieldID, this));
							}
						}
					}
				}
				//
				// Analyze if adding blank row is allowed //
				if ((headerTable.getKeyFieldIDList().size() + 1) == detailTable.getKeyFieldIDList().size() && !detailTable.getDetailRowNoID().equals("")) {
					isAbleToSetupKeysOfNewRecord = true;
				} else {
					isAbleToSetupKeysOfNewRecord = false;
				}

				/////////////////////////////////////
				// Setup Function Keys and Buttons //
				/////////////////////////////////////
				setupFunctionKeysAndButtons();

				///////////////////////////////
				// Setup Add-Row-List Dialog //
				///////////////////////////////
				if (functionElement_.getAttribute("AddRowListTable").equals("")) {
					addRowListDialog = null;
				} else {
					addRowListDialog = new XF310_AddRowList(this);
					if (addRowListDialog.isInvalid()) {
						throw new Exception();
					}
				}

				////////////////////////////////////////////////
				// Select Detail Records and Setup Table rows //
				////////////////////////////////////////////////
				selectDetailRecordsAndSetupTableRows();

				////////////////
				// Show Panel //
				////////////////
				if (parmMap_.containsKey("INITIAL_MESSAGE")) {
					jTextAreaMessages.setText((String)parmMap_.get("INITIAL_MESSAGE"));
				} else {
					if (initialMsg.equals("")) {
						jTextAreaMessages.setText(res.getString("FunctionMessage7") + buttonUpdateCaption + res.getString("FunctionMessage8"));
					} else {
						jTextAreaMessages.setText(initialMsg);
					}
				}
				if (firstEditableHeaderField != null) {
					firstEditableHeaderField.requestFocus();
				}
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				this.setVisible(true);
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError5"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		///////////////////////////////
		// Release instance and exit //
		///////////////////////////////
		return returnMap_;
	}

	public boolean isAvailable() {
		return instanceIsAvailable;
	}
	
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			closeFunction();
		}
	}

	void setErrorAndCloseFunction() {
		isToBeCanceled = true;
		returnMap_.put("RETURN_CODE", "99");
		closeFunction();
	}

	void closeFunction() {
		instanceIsAvailable = true;
		//
		if (anyRecordsDeleted && !returnMap_.get("RETURN_CODE").toString().equals("99")) {
			returnMap_.put("RETURN_CODE", "20");
		}
		//
		String wrkStr;
		if (exceptionLog.size() > 0 || !exceptionHeader.equals("")) {
			wrkStr = processLog.toString() + "\nERROR LOG:\n" + exceptionHeader + exceptionLog.toString();
		} else {
			wrkStr = processLog.toString();
		}
		wrkStr = wrkStr.replace("'", "\"");
		session_.writeLogOfFunctionClosed(programSequence, returnMap_.get("RETURN_CODE").toString(), wrkStr);
		this.setVisible(false);
	}
	
	public void cancelWithMessage(String message) {
		if (!message.equals("")) {
			JOptionPane.showMessageDialog(null, message);
		}
		returnMap_.put("RETURN_CODE", "21");
		isToBeCanceled = true;
	}
	
	public void callFunction(String functionID) {
		try {
			returnMap_ = XFUtility.callFunction(session_, functionID, parmMap_);
		} catch (Exception ex) {
			String message = res.getString("FunctionError9") + functionID + res.getString("FunctionError10");
			JOptionPane.showMessageDialog(null,message);
			exceptionHeader = message;
			setErrorAndCloseFunction();
		}
	}

	void fetchHeaderRecord() {
		try {
			ResultSet resultOfHeaderTable;
			Statement statementForHeaderTable = connection.createStatement();
			//
			boolean recordNotFound = true;
			String inputDialogMessage = "";
			boolean keyInputRequired = false;
			for (int i = 0; i < headerFieldList.size(); i++) {
				if (headerFieldList.get(i).isKey()) {
					if (parmMap_.containsKey(headerFieldList.get(i).getFieldID())) {
						if (parmMap_.get(headerFieldList.get(i).getFieldID()).equals(headerFieldList.get(i).getNullValue())) {
							keyInputRequired = true;
						}
					} else {
						keyInputRequired = true;
					}
				} else {
					headerFieldList.get(i).setValue(headerFieldList.get(i).getNullValue());
				}
			}
			//
			while (recordNotFound) {
				if (keyInputRequired) {
					if (keyInputDialog == null) {
						keyInputDialog = new XF310_KeyInputDialog(this);
					}
					parmMap_ = keyInputDialog.requestKeyValues(inputDialogMessage);
					if (parmMap_.size() == 0) {
						isToBeCanceled = true;
						returnMap_.put("RETURN_CODE", "01");
						closeFunction();
						return;
					}
				}
				//
				headerTable.runScript("BR", ""); /* Script to be run BEFORE READ */
				//
				String sql = headerTable.getSQLToSelect();
				XFUtility.appendLog(sql, processLog);
				resultOfHeaderTable = statementForHeaderTable.executeQuery(sql);
				if (resultOfHeaderTable.next()) {
					//
					recordNotFound = false;
					//
					for (int i = 0; i < headerFieldList.size(); i++) {
						if (headerFieldList.get(i).getTableID().equals(headerTable.getTableID())) {
							headerFieldList.get(i).setValueOfResultSet(resultOfHeaderTable);
						}
					}
					//
					headerTable.setUpdateCounterValue(resultOfHeaderTable);
					//
					resultOfHeaderTable.close();
					//
				} else {
					//
					resultOfHeaderTable.close();
					//
					if (keyInputRequired) {
						inputDialogMessage = res.getString("FunctionError37");
					} else {
						JOptionPane.showMessageDialog(this, res.getString("FunctionError38"));
						isToBeCanceled = true;
						returnMap_.put("RETURN_CODE", "01");
						closeFunction();
						return;
					}
				}
			}
			//
			fetchHeaderReferRecords("AR", false, "");
			//
			headerTable.runScript("BU", "AR()"); /* Script to be run BEFORE UPDATE */
			//
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError6"));
			ex.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} catch(ScriptException ex) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError7") + this.getScriptNameRunning() + res.getString("FunctionError8"));
			exceptionHeader = "'" + this.getScriptNameRunning() + "' Script error\n";
			ex.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
	}
	
	int fetchHeaderReferRecords(String event, boolean toBeChecked, String specificReferTable) {
		int countOfErrors = 0;
		boolean recordNotFound;
		ResultSet resultOfReferTable;
		String sql;
		//
		try {
			Statement statementForReferTable = connection.createStatement();
			//
			countOfErrors = countOfErrors + headerTable.runScript(event, "BR()"); /* Script to be run BEFORE READ */
			//
			for (int i = 0; i < headerReferTableList.size(); i++) {
				if (specificReferTable.equals("") || specificReferTable.equals(headerReferTableList.get(i).getTableAlias())) {
					//
					if (headerReferTableList.get(i).isToBeExecuted()) {
						//
						countOfErrors = countOfErrors + headerTable.runScript(event, "BR(" + headerReferTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE READ */
						//
						if (!headerReferTableList.get(i).isKeyNullable() || !headerReferTableList.get(i).isKeyNull()) {
							//
							recordNotFound = true;
							//
							sql = headerReferTableList.get(i).getSelectSQL(false);
							XFUtility.appendLog(sql, processLog);
							resultOfReferTable = statementForReferTable.executeQuery(sql);
							while (resultOfReferTable.next()) {
								//
								if (headerReferTableList.get(i).isRecordToBeSelected(resultOfReferTable)) {
									//
									recordNotFound = false;
									//
									for (int j = 0; j < headerFieldList.size(); j++) {
										if (headerFieldList.get(j).getTableAlias().equals(headerReferTableList.get(i).getTableAlias())) {
											headerFieldList.get(j).setValueOfResultSet(resultOfReferTable);
										}
									}
									//
									countOfErrors = countOfErrors + headerTable.runScript(event, "AR(" + headerReferTableList.get(i).getTableAlias() + ")"); /* Script to be run AFTER READ */
								}
							}
							//
							if (recordNotFound && toBeChecked && !headerReferTableList.get(i).isOptional()) {
								countOfErrors++;
								headerReferTableList.get(i).setErrorOnRelatedFields();
							}
							//
							resultOfReferTable.close();
						}
					}
				}
			}
			//
			countOfErrors = countOfErrors + headerTable.runScript(event, "AR()"); /* Script to be run AFTER READ */
			//
			// check if prompt-key is EditControlled or not //
			for (int i = 0; i < headerFieldList.size(); i++) {
				headerFieldList.get(i).checkPromptKeyEdit();
			}
			//
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError6"));
			ex.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} catch(ScriptException ex) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError7") + this.getScriptNameRunning() + res.getString("FunctionError8"));
			exceptionHeader = "'" + this.getScriptNameRunning() + "' Script error\n";
			ex.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
		//
		return countOfErrors;
	}
	
	int fetchDetailReferRecords(String event, boolean toBeChecked, String specificReferTable, HashMap<String, Object> columnValueMap, HashMap<String, Object> columnOldValueMap) {
		int countOfErrors = 0;
		boolean recordNotFound;
		ResultSet resultOfDetailReferTable;
		String sql;
		//
		try {
			Statement statementForReferTable = connection.createStatement();
			//
			countOfErrors = countOfErrors + detailTable.runScript(event, "BR()", columnValueMap, columnOldValueMap); /* Script to be run AFTER READ primary table */
			//
			for (int i = 0; i < detailReferTableList.size(); i++) {
				if (specificReferTable.equals("") || specificReferTable.equals(detailReferTableList.get(i).getTableAlias())) {
					//
					if (detailReferTableList.get(i).isToBeExecuted()) {
						//
						countOfErrors = countOfErrors + detailTable.runScript(event, "BR(" + detailReferTableList.get(i).getTableAlias() + ")", columnValueMap, columnOldValueMap); /* Script to be run BEFORE READ */
						//
						if (!detailReferTableList.get(i).isKeyNullable() || !detailReferTableList.get(i).isKeyNull()) {
							//
							recordNotFound = true;
							//
							sql = detailReferTableList.get(i).getSelectSQL(false);
							if (!sql.equals("")) {
								XFUtility.appendLog(sql, processLog);
								resultOfDetailReferTable = statementForReferTable.executeQuery(sql);
								while (resultOfDetailReferTable.next()) {
									//
									if (detailReferTableList.get(i).isRecordToBeSelected(resultOfDetailReferTable)) {
										//
										recordNotFound = false;
										//
										for (int j = 0; j < detailColumnList.size(); j++) {
											if (detailColumnList.get(j).getTableAlias().equals(detailReferTableList.get(i).getTableAlias())) {
												detailColumnList.get(j).setValueOfResultSet(resultOfDetailReferTable);
												columnValueMap.put(detailColumnList.get(j).getDataSourceName(), detailColumnList.get(j).getInternalValue());
											}
										}
										//
										countOfErrors = countOfErrors + detailTable.runScript(event, "AR(" + detailReferTableList.get(i).getTableAlias() + ")", columnValueMap, columnOldValueMap); /* Script to be run AFTER READ */
									}
								}
								//
								resultOfDetailReferTable.close();
							}
							//
							if (recordNotFound && toBeChecked && !detailReferTableList.get(i).isOptional()) {
								countOfErrors++;
								detailReferTableList.get(i).setErrorOnRelatedFields();
							}
						}
					}
				}
			}
			//
			countOfErrors = countOfErrors + detailTable.runScript(event, "AR()", columnValueMap, columnOldValueMap); /* Script to be run AFTER READ */
			//
			for (int i = 0; i < detailColumnList.size(); i++) {
				detailColumnList.get(i).checkPromptKeyEdit();
			}
			//
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError6"));
			ex.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} catch(ScriptException ex) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError7") + this.getScriptNameRunning() + res.getString("FunctionError8"));
			exceptionHeader = "'" + this.getScriptNameRunning() + "' Script error\n";
			ex.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
		//
		return countOfErrors;
	}

	void setupFunctionKeysAndButtons() {
		//
		InputMap inputMap  = jPanelCenter.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.clear();
		ActionMap actionMap = jPanelCenter.getActionMap();
		actionMap.clear();
		//
		for (int i = 0; i < 7; i++) {
			jButtonArray[i].setText("");
			jButtonArray[i].setVisible(false);
			actionDefinitionArray[i] = "";
		}
		//
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CHECK");
		actionMap.put("CHECK", checkAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
		actionMap.put("HELP", helpAction);
		//
		int workIndex;
		org.w3c.dom.Element element;
		NodeList buttonList = functionElement_.getElementsByTagName("Button");
		for (int i = 0; i < buttonList.getLength(); i++) {
			element = (org.w3c.dom.Element)buttonList.item(i);
			//
			workIndex = Integer.parseInt(element.getAttribute("Position"));
			actionDefinitionArray[workIndex] = element.getAttribute("Action");
			XFUtility.setCaptionToButton(jButtonArray[workIndex], element, "");
			jButtonArray[workIndex].setVisible(true);
			inputMap.put(XFUtility.getKeyStroke(element.getAttribute("Number")), "actionButton" + workIndex);
			actionMap.put("actionButton" + workIndex, actionButtonArray[workIndex]);
			//
			if (element.getAttribute("Action").equals("ADD_ROW")) {
				addRowListTitle = element.getAttribute("Caption");
			}
			if (element.getAttribute("Action").equals("UPDATE")) {
				buttonUpdateCaption = element.getAttribute("Caption");
				keyStrokeToUpdate = XFUtility.getKeyStroke(element.getAttribute("Number"));
			}
		}
	}
	
	int getKeyCodeToUpdate() {
		return keyStrokeToUpdate.getKeyCode();
	}

	void selectDetailRecordsAndSetupTableRows() {
		HashMap<String, Object> keyValueMap;
		HashMap<String, Object> columnValueMap;
		HashMap<String, Boolean> columnIsEditableMap;
		ArrayList<Object> orderByValueList;
		String workStr;
		// 
		try {
			//
			int rowCount = tableModelMain.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				tableModelMain.removeRow(0);
			}
			tableRowList.clear();
			//
			ResultSet resultOfDetailTable;
			Statement statementForDetailTable = connection.createStatement();
			//
			int countOfRows = 0;
			String sql = detailTable.getSQLToSelect();
			XFUtility.appendLog(sql, processLog);
			resultOfDetailTable = statementForDetailTable.executeQuery(sql);
			while (resultOfDetailTable.next()) {
				//
				for (int i = 0; i < detailColumnList.size(); i++) {
					detailColumnList.get(i).setEditable(true);
					detailColumnList.get(i).setValue(detailColumnList.get(i).getNullValue());
				}
				//
				detailTable.runScript("BR", "", null, null); /* Detail Table Script to be run BEFORE READ */
				//
				for (int i = 0; i < detailColumnList.size(); i++) {
					//detailColumnList.get(i).initValue();
					if (detailColumnList.get(i).getTableID().equals(detailTable.getTableID())) {
						detailColumnList.get(i).setValueOfResultSet(resultOfDetailTable);
					}
				}
				//
				columnValueMap = new HashMap<String, Object>();
				keyValueMap = new HashMap<String, Object>();
				for (int i = 0; i < detailColumnList.size(); i++) {
					columnValueMap.put(detailColumnList.get(i).getDataSourceName(), detailColumnList.get(i).getInternalValue());
					if (detailColumnList.get(i).isKey()) {
						keyValueMap.put(detailColumnList.get(i).getFieldID(), detailColumnList.get(i).getInternalValue());
					}
				}
				columnValueMap.put(detailTable.getUpdateCounterID(), resultOfDetailTable.getInt(detailTable.getUpdateCounterID()));
				//
				fetchDetailReferRecords("AR", false, "", columnValueMap, null);
				//
				detailTable.runScript("BU", "AR()", columnValueMap, null); /* Detail Table Script to be run BEFORE UPDATE */
				//
				if (detailTable.hasOrderByAsItsOwnFields()) {
					int columnIndex;
					Object[] Cell = new Object[detailColumnList.size() + 1];
					Cell[0] = new XF310_DetailRowNumber(countOfRows + 1, "CURRENT", keyValueMap, columnValueMap, this);
					for (int i = 0; i < detailColumnList.size(); i++) {
						if (detailColumnList.get(i).isVisibleOnPanel()) {
							columnIndex = detailColumnList.get(i).getColumnIndex();
							Cell[columnIndex] = new XF310_DetailCell(detailColumnList.get(i), (XF310_DetailRowNumber)Cell[0]);
							if (detailColumnList.get(i).isEditable()) {
								tableModelMain.setEditable(countOfRows, columnIndex);
							}
						}
					}
					tableModelMain.addRow(Cell);
				} else {
					columnIsEditableMap = new HashMap<String, Boolean>();
					for (int i = 0; i < detailColumnList.size(); i++) {
						columnIsEditableMap.put(detailColumnList.get(i).getDataSourceName(), detailColumnList.get(i).isEditable());
					}
					orderByValueList = new ArrayList<Object>();
					for (int i = 0; i < detailTable.getOrderByFieldIDList().size(); i++) {
						workStr = detailTable.getOrderByFieldIDList().get(i).replace("(D)", "");
						workStr = workStr.replace("(A)", "");
						for (int j = 0; j < detailColumnList.size(); j++) {
							if (detailColumnList.get(j).getDataSourceName().equals(workStr)) {
								orderByValueList.add(detailColumnList.get(j).getExternalValue());
								break;
							}
						}
					}
					tableRowList.add(new WorkingRow(keyValueMap, columnValueMap, orderByValueList, columnIsEditableMap));
				}
				//
				countOfRows++;
			}
			//
			resultOfDetailTable.close();
			//
			headerTable.runScript("BU", "AS()"); /* Header Table Script to be run BEFORE UPDATE and AFTER SUMMARY */
			//
			if (!detailTable.hasOrderByAsItsOwnFields()) {
				WorkingRow[] workingRowArray = tableRowList.toArray(new WorkingRow[0]);
				Arrays.sort(workingRowArray, new WorkingRowComparator());
				for (int i = 0; i < workingRowArray.length; i++) {
					int columnIndex;
					Object[] Cell = new Object[detailColumnList.size() + 1];
					Cell[0] = new XF310_DetailRowNumber(i + 1, "CURRENT", workingRowArray[i].getKeyValueMap(), workingRowArray[i].getColumnValueMap(), this);
					for (int j = 0; j < detailColumnList.size(); j++) {
						if (detailColumnList.get(j).isVisibleOnPanel()) {
							columnIndex = detailColumnList.get(j).getColumnIndex(); 
							Cell[columnIndex] = new XF310_DetailCell(detailColumnList.get(j), (XF310_DetailRowNumber)Cell[0]);
							if (workingRowArray[i].getColumnIsEditableMap().get(detailColumnList.get(j).getDataSourceName())) {
								tableModelMain.setEditable(i, columnIndex);
							}
						}
					}
					tableModelMain.addRow(Cell);
				}
			}
			//
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError6"));
			ex.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} catch(ScriptException ex) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError7") + this.getScriptNameRunning() + res.getString("FunctionError8"));
			exceptionHeader = "'" + this.getScriptNameRunning() + "' Script error\n";
			ex.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
	}

	void checkErrorsToUpdate(boolean checkOnly) {
		XF310_DetailRowNumber tableRowNumber;
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			//
			if (activeCellEditor != null) {
				activeCellEditor.stopCellEditing();
				activeCellEditor = null;
			}
			//
			int countOfErrors = 0;
			//
			for (int i = 0; i < headerFieldList.size(); i++) {
				headerFieldList.get(i).setError(false);
			}
			//
			countOfErrors = fetchHeaderReferRecords("BU", true, "");
			//
			for (int i = 0; i < headerFieldList.size(); i++) {
				if (headerFieldList.get(i).isFieldOnPrimaryTable()) {
					if (headerFieldList.get(i).isNullError()) {
						countOfErrors++;
					}
				}
			}
			//
			for (int i = 0; i < jTableMain.getRowCount(); i++) {
				tableRowNumber = (XF310_DetailRowNumber)tableModelMain.getValueAt(i, 0);
				tableRowNumber.setValueToDetailColumns();
				countOfErrors = countOfErrors + tableRowNumber.countErrors(messageList);
			}
			//
			countOfErrors = countOfErrors + headerTable.runScript("BU", "AS()"); /* Script to be run BEFORE UPDATE and AFTER SUMMARY */
			//
			if (countOfErrors == 0) {
				//
				if (hasNoErrorWithKey()) {
					//
					if (checkOnly) {
						messageList.add(res.getString("FunctionMessage9"));
					} else {
						//
						Statement statement = connection.createStatement();
						String sql;
						int recordCount;
						//
						///////////////////////////////////////////
						// Delete detail records of removed rows //
						///////////////////////////////////////////
						for (int i = 0; i < deleteRowNumberList.size(); i++) {
							tableRowNumber = (XF310_DetailRowNumber)deleteRowNumberList.get(i);
							tableRowNumber.setValueToDetailColumns();
							DeleteChecker deleteChecker = new DeleteChecker(session_, detailTable.getTableElement(), deleteRowNumberList.get(i).getKeyValueMap(), scriptEngine);
							ArrayList<String> errorMsgList = deleteChecker.getDeleteErrors();
							if (errorMsgList.size() > 0) {
								StringBuffer buf = new StringBuffer();
								for (int j = 0; j < errorMsgList.size(); j++) {
									if (j > 0) {
										buf.append("\n");
									}
									buf.append(errorMsgList.get(j));
								}
								JOptionPane.showMessageDialog(jPanelMain, buf.toString());
								exceptionHeader = buf.toString();
								try {
									connection.rollback();
								} catch (SQLException e) {
									e.printStackTrace(exceptionStream);
								}
								setErrorAndCloseFunction();
							}
							//
							sql = detailTable.getSQLToDelete(deleteRowNumberList.get(i).getKeyValueMap(), (Integer)deleteRowNumberList.get(i).getColumnValueMap().get(detailTable.getUpdateCounterID()));
							XFUtility.appendLog(sql, processLog);
							recordCount = statement.executeUpdate(sql);
							if (recordCount == 1) {
								detailTable.runScript("AD", "", deleteRowNumberList.get(i).getColumnValueMap(), deleteRowNumberList.get(i).getColumnOldValueMap());
								anyRecordsDeleted = true;
							} else {
								String errorMessage = res.getString("FunctionError33");
								JOptionPane.showMessageDialog(jPanelMain, errorMessage);
								exceptionHeader = errorMessage;
								try {
									connection.rollback();
								} catch (SQLException e) {
									e.printStackTrace(exceptionStream);
								}
								setErrorAndCloseFunction();
							}
						}
						//
						//////////////////////////////
						// Update the header record //
						//////////////////////////////
						sql = headerTable.getSQLToUpdate();
						XFUtility.appendLog(sql, processLog);
						recordCount = statement.executeUpdate(sql);
						if (recordCount == 1) {
							headerTable.runScript("AU", "");
							//
							///////////////////////////////
							// Update the detail records //
							///////////////////////////////
							for (int i = 0; i < jTableMain.getRowCount(); i++) {
								int rowNumber = jTableMain.convertRowIndexToModel(i);
								if (rowNumber > -1) {
									tableRowNumber = (XF310_DetailRowNumber)tableModelMain.getValueAt(rowNumber,0);
									tableRowNumber.setValueToDetailColumns();
									if (tableRowNumber.getRecordType().equals("CURRENT")) {
										sql = detailTable.getSQLToUpdate(tableRowNumber);
										XFUtility.appendLog(sql, processLog);
										recordCount = statement.executeUpdate(sql);
										if (recordCount == 1) {
											detailTable.runScript("AU", "", tableRowNumber.getColumnValueMap(), tableRowNumber.getColumnOldValueMap());
										} else {
											String errorMessage = res.getString("FunctionError19");
											JOptionPane.showMessageDialog(jPanelMain, errorMessage);
											exceptionHeader = errorMessage;
											try {
												connection.rollback();
											} catch (SQLException e) {
												e.printStackTrace(exceptionStream);
											}
											setErrorAndCloseFunction();
										}
									}
									if (tableRowNumber.getRecordType().equals("NEW")) {
										sql = detailTable.getSQLToInsert(tableRowNumber);
										XFUtility.appendLog(sql, processLog);
										recordCount = statement.executeUpdate(sql);
										if (recordCount == 1) {
											detailTable.runScript("AC", "", tableRowNumber.getColumnValueMap(), tableRowNumber.getColumnOldValueMap());
										}
									}
								}
							}
							//
							returnMap_.put("RETURN_CODE", "20");
							//
						} else {
							String errorMessage = res.getString("FunctionError19");
							JOptionPane.showMessageDialog(jPanelMain, errorMessage);
							exceptionHeader = errorMessage;
							try {
								connection.rollback();
							} catch (SQLException e) {
								e.printStackTrace(exceptionStream);
							}
							setErrorAndCloseFunction();
						}
						//
						closeFunction();
					}
				}
			}
			//
			if (this.isToBeCanceled) {
				connection.rollback();
				closeFunction();
			} else {
				connection.commit();
			}
			//
		} catch (SQLException e1) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError6"));
			e1.printStackTrace(exceptionStream);
			try {
				connection.rollback();
			} catch (SQLException e2) {
				e2.printStackTrace(exceptionStream);
			}
			setErrorAndCloseFunction();
			//
		} catch(ScriptException e1) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError7") + this.getScriptNameRunning() + res.getString("FunctionError8"));
			e1.printStackTrace(exceptionStream);
			exceptionHeader = "'" + this.getScriptNameRunning() + "' Script error\n";
			try {
				connection.rollback();
			} catch (SQLException e2) {
				e2.printStackTrace(exceptionStream);
			}
			setErrorAndCloseFunction();
			//
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	boolean hasNoErrorWithKey() {
		boolean hasNoError = true;
		ArrayList<String> uniqueKeyList = new ArrayList<String>();
		ArrayList<String> keyFieldList = new ArrayList<String>();
		ResultSet resultOfPrimaryTable = null;
		StringTokenizer workTokenizer;
		String sql = "";
		//
		try {
			Statement statement = connection.createStatement();
			//
			if (headerTable.hasPrimaryKeyValueAltered()) {
				hasNoError = false;
				messageList.add(res.getString("FunctionError43"));
				for (int i = 0; i < headerFieldList.size(); i++) {
					if (headerFieldList.get(i).isKey()) {
						headerFieldList.get(i).setError(true);
					}
				}
			}
			//
			if (hasNoError) {
				uniqueKeyList = headerTable.getUniqueKeyList();
				for (int i = 0; i < uniqueKeyList.size(); i++) {
					keyFieldList.clear();
					workTokenizer = new StringTokenizer(uniqueKeyList.get(i), ";" );
					while (workTokenizer.hasMoreTokens()) {
						keyFieldList.add(workTokenizer.nextToken());
					}
					sql = headerTable.getSQLToCheckSKDuplication(keyFieldList);
					XFUtility.appendLog(sql, processLog);
					resultOfPrimaryTable = statement.executeQuery(sql);
					if (resultOfPrimaryTable.next()) {
						hasNoError = false;
						messageList.add(res.getString("FunctionError22"));
						for (int j = 0; j < headerFieldList.size(); j++) {
							if (keyFieldList.contains(headerFieldList.get(j).getFieldID())) {
								headerFieldList.get(j).setError(true);
							}
						}
					}
				}
			}
			//
			uniqueKeyList = detailTable.getUniqueKeyList();
			int rowNumber;
			for (int j = 0; j < jTableMain.getRowCount(); j++) {
				XF310_DetailRowNumber tableRowNumber = (XF310_DetailRowNumber)tableModelMain.getValueAt(j, 0);
				rowNumber = tableRowNumber.getRowIndex() + 1;
				//
				if (tableRowNumber.getRecordType().equals("CURRENT")) {
					if (detailTable.hasPrimaryKeyValueAltered(tableRowNumber)) {
						hasNoError = false;
						messageList.add(res.getString("FunctionError26") + rowNumber + res.getString("FunctionError27"));
					}
				}
				//
				if (hasNoError) {
					for (int i = 0; i < uniqueKeyList.size(); i++) {
						keyFieldList.clear();
						workTokenizer = new StringTokenizer(uniqueKeyList.get(i), ";" );
						while (workTokenizer.hasMoreTokens()) {
							keyFieldList.add(workTokenizer.nextToken());
						}
						if (tableRowNumber.getRecordType().equals("CURRENT")) {
							sql = detailTable.getSQLToCheckSKDuplication(tableRowNumber, keyFieldList, true);
						} else {
							sql = detailTable.getSQLToCheckSKDuplication(tableRowNumber, keyFieldList, false);
						}
						XFUtility.appendLog(sql, processLog);
						resultOfPrimaryTable = statement.executeQuery(sql);
						if (resultOfPrimaryTable.next()) {
							hasNoError = false;
							messageList.add(res.getString("FunctionError28") + rowNumber + res.getString("FunctionError29"));
						}
						if (!hasNoError) {
							break;
						}
					}
				} else {
					break;
				}
			}
			//
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError6"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} finally {
			try {
				if (resultOfPrimaryTable != null) {
					resultOfPrimaryTable.close();
				}
			} catch (SQLException e) {
				e.printStackTrace(exceptionStream);
			}
		}
		//
		return hasNoError;
	}
	
	boolean isAbleToSetupKeysOfNewRecord() {
		return isAbleToSetupKeysOfNewRecord;
	}
	
	void addRow() {
		try {
			//
			////////////////////////////////
			//Get latest detail row number//
			////////////////////////////////
			if (!detailTable.getDetailRowNoID().equals("")) {
				detailRowNoLastValue = 0;
				for (int i = 0; i < jTableMain.getRowCount(); i++) {
					int rowNumber = jTableMain.convertRowIndexToModel(i);
					if (rowNumber > -1) {
						XF310_DetailRowNumber tableRowNumber = (XF310_DetailRowNumber)tableModelMain.getValueAt(rowNumber,0);
						int rowNoValue = (Integer)tableRowNumber.getKeyValueMap().get(detailTable.getDetailRowNoID());
						if (rowNoValue > detailRowNoLastValue) {
							detailRowNoLastValue = rowNoValue;
						}
					}
				}
			}
			//
			if (addRowListDialog == null) {
				//
				if (isAbleToSetupKeysOfNewRecord) {
					setupNewRowAndAddToJTable(null);
					//if (initialMsg.equals("")) {
					//	messageList.add(res.getString("FunctionMessage7") + buttonUpdateCaption + res.getString("FunctionMessage8"));
					//} else {
					//	messageList.add(initialMsg);
					//}
				} else {
					messageList.add(res.getString("FunctionError43"));
				}
				//
			} else {
				//
				ArrayList<XF310_AddRowListNumber> addRowListNumberList = addRowListDialog.getDefaultRow();
				if (addRowListNumberList == null) {
					int result = addRowListDialog.requestSelection();
					if (result == 0) {
						messageList.add(res.getString("FunctionMessage33"));
					}
					if (result == 1) {
						addRowListNumberList = addRowListDialog.getSelectionList();
					}
					if (result == 2) {
						setupNewRowAndAddToJTable(null);
						//if (initialMsg.equals("")) {
						//	messageList.add(res.getString("FunctionMessage7") + buttonUpdateCaption + res.getString("FunctionMessage8"));
						//} else {
						//	messageList.add(initialMsg);
						//}
					}
					if (result == 3) {
						closeFunction();
					}
				}
				//
				if (addRowListNumberList != null) {
					int countOfAdded = 0;
					for (int i = 0; i < addRowListNumberList.size(); i++) {
						countOfAdded = countOfAdded + setupNewRowAndAddToJTable(addRowListNumberList.get(i));
					}
					int countOfNotAdded = addRowListNumberList.size() - countOfAdded;
					if (countOfNotAdded > 0) {
						messageList.add(res.getString("FunctionMessage34") + countOfNotAdded + res.getString("FunctionMessage35"));
					//} else {
						//if (initialMsg.equals("")) {
						//	messageList.add(res.getString("FunctionMessage7") + buttonUpdateCaption + res.getString("FunctionMessage8"));
						//} else {
						//	messageList.add(initialMsg);
						//}
					}
				}
			}
			//
			checkErrorsToUpdate(true);
			//
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError5"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
	}
	
	int setupNewRowAndAddToJTable(XF310_AddRowListNumber rowNumber) {
		XF310_HeaderField headerField;
		XF310_DetailColumn detailColumn;
		int countOfAdded = 0;
		//
		detailRowNoLastValue++;
		//
		// Initialize field values //
		for (int i = 0; i < detailColumnList.size(); i++) {
			if (detailColumnList.get(i).getTableID().equals(detailTable.getTableID())
					&& detailColumnList.get(i).getFieldID().equals(detailTable.getDetailRowNoID())) {
				detailColumnList.get(i).setValue(detailRowNoLastValue);
			} else {
				if (detailColumnList.get(i).isAutoNumberField()) {
					detailColumnList.get(i).setValue(detailColumnList.get(i).getAutoNumberValue());
				} else {
					detailColumnList.get(i).setValue(detailColumnList.get(i).getNullValue());
				}
			}
		}
		//
		// Set values from Header Key or Add-Row-List //
		for (int i = 0; i < headerTable.getKeyFieldIDList().size(); i++) {
			headerField = getHeaderFieldObjectByID(headerTable.getTableID(), "", headerTable.getKeyFieldIDList().get(i));
			detailColumn = getDetailColumnObjectByID(detailTable.getTableID(), "", detailTable.getKeyFieldIDList().get(i));
			detailColumn.setValue(headerField.getInternalValue());
		}
		if (rowNumber != null) {
			for (int i = 0; i < detailColumnList.size(); i++) {
				if (rowNumber.getReturnFieldMap().containsKey(detailColumnList.get(i).getDataSourceName())) {
					detailColumnList.get(i).setValue(rowNumber.getReturnFieldMap().get(detailColumnList.get(i).getDataSourceName()));
				}
			}
		}
		//
		// Setup column-map(DataSourceName, Value) and key-map(FieldID, Value) //
		HashMap<String, Object> columnValueMap = new HashMap<String, Object>();
		HashMap<String, Object> keyValueMap = new HashMap<String, Object>();
		for (int i = 0; i < detailColumnList.size(); i++) {
			columnValueMap.put(detailColumnList.get(i).getDataSourceName(), detailColumnList.get(i).getInternalValue());
			if (detailColumnList.get(i).isKey()) {
				keyValueMap.put(detailColumnList.get(i).getFieldID(), detailColumnList.get(i).getInternalValue());
			}
		}
		//
		fetchDetailReferRecords("BC", false, "", columnValueMap, null);
		//
		boolean duplicatedKey = false;
		int wrkInt;
		XF310_DetailRowNumber wrkRowNumber;
		for (int i = 0; i < jTableMain.getRowCount(); i++) {
			wrkInt = jTableMain.convertRowIndexToModel(i);
			if (wrkInt > -1) {
				wrkRowNumber = (XF310_DetailRowNumber)tableModelMain.getValueAt(wrkInt,0);
				if (wrkRowNumber.getKeyValueMap().equals(keyValueMap)) {
					duplicatedKey = true;
				}
			}
		}
		if (duplicatedKey) {
			countOfAdded = 0;
		} else {
			int columnIndex;
			Object[] Cell = new Object[detailColumnList.size() + 1];
			Cell[0] = new XF310_DetailRowNumber(tableModelMain.getRowCount() + 1, "NEW", keyValueMap, columnValueMap, this);
			for (int i = 0; i < detailColumnList.size(); i++) {
				if (detailColumnList.get(i).isVisibleOnPanel()) {
					columnIndex = detailColumnList.get(i).getColumnIndex();
					Cell[columnIndex] = new XF310_DetailCell(detailColumnList.get(i), (XF310_DetailRowNumber)Cell[0]);
					if (detailColumnList.get(i).isEditable()) {
						tableModelMain.setEditable(tableModelMain.getRowCount(), columnIndex);
					}
				}
			}
			//
			tableModelMain.addRow(Cell);
			countOfAdded = 1;
		}
		//
		return countOfAdded;
	}
	
	void removeRow() {
		XF310_DetailCell cell;
		//
		int rowIndex = jTableMain.getSelectedRow();
		if (rowIndex == -1) {
			messageList.add(res.getString("FunctionError44"));
		} else {
			//
			if (activeCellEditor != null) {
				activeCellEditor.stopCellEditing();
				activeCellEditor = null;
			}
			//
			XF310_DetailRowNumber rowNumber = (XF310_DetailRowNumber)tableModelMain.getValueAt(rowIndex, 0);
			//
			Object[] bts = {res.getString("Yes"), res.getString("No")} ;
			int reply = JOptionPane.showOptionDialog(this, res.getString("FunctionMessage36") + rowNumber.toString() + res.getString("FunctionMessage37"), res.getString("CheckToDelete"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[0]);
			if (reply == 0) {
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));
					//
					// Reset Error Status //
					for (int i = 0; i < detailColumnList.size(); i++) {
						detailColumnList.get(i).setValue(rowNumber.getColumnValueMap().get(detailColumnList.get(i).getDataSourceName()));
						detailColumnList.get(i).setError(false);
						if (detailColumnList.get(i).isVisibleOnPanel()) {
							cell = (XF310_DetailCell)tableModelMain.getValueAt(rowIndex, detailColumnList.get(i).getColumnIndex());
							cell.setError(false);
						}
					}
					//
					int countOfErrors = fetchDetailReferRecords("BD", false, "", rowNumber.getColumnValueMap(), rowNumber.getColumnOldValueMap());
					//
					countOfErrors = countOfErrors + headerTable.runScript("BD", "AS()"); /* Script to be run AFTER SUMMARY */
					//
					if (countOfErrors != 0)	{
						//
						// Set Errors on Cells //
						for (int i = 0; i < detailColumnList.size(); i++) {
							if (detailColumnList.get(i).isVisibleOnPanel() && detailColumnList.get(i).isEditable()) {
								cell = (XF310_DetailCell)tableModelMain.getValueAt(rowIndex, detailColumnList.get(i).getColumnIndex());
								if (detailColumnList.get(i).isError()) {
									cell.setError(true);
								} else {
									cell.setError(false);
								}
							}
						}
						//
					} else {
						//
						if (rowNumber.getRecordType().equals("CURRENT")) {
							DeleteChecker deleteChecker = new DeleteChecker(session_, detailTable.getTableElement(), rowNumber.getKeyValueMap(), scriptEngine);
							ArrayList<String> errorMsgList = deleteChecker.getDeleteErrors();
							for (int i = 0; i < errorMsgList.size(); i++) {
								messageList.add(errorMsgList.get(i));
							}
							//
							countOfErrors = errorMsgList.size();
						}
						//
						if (countOfErrors == 0) {
							if (rowNumber.getRecordType().equals("CURRENT")) {
								deleteRowNumberList.add(rowNumber);
							}
							//
							tableModelMain.removeRow(rowIndex);
							//messageList.add(res.getString("FunctionMessage38"));
							checkErrorsToUpdate(true);
						}
						//
					}
				} catch(Exception e) {
					e.printStackTrace(exceptionStream);
					setErrorAndCloseFunction();
				} finally {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			} else {
				messageList.add(res.getString("FunctionMessage39"));
			}
		}
	}

	void jFunctionButton_actionPerformed(ActionEvent e) {
		Component com = (Component)e.getSource();
		for (int i = 0; i < 7; i++) {
			if (com.equals(jButtonArray[i])) {
				doButtonAction(actionDefinitionArray[i]);
				break;
			}
		}
	}

	void doButtonAction(String action) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			//
			messageList.clear();
			//
			if (action.equals("EXIT")) {
				closeFunction();
			}
			//
			if (action.equals("UPDATE")) {
				checkErrorsToUpdate(false);
			}
			//
			if (action.equals("ADD_ROW")) {
				addRow();
			}
			//
			if (action.equals("REMOVE_ROW")) {
				removeRow();
			}
			//
			if (action.equals("OUTPUT")) {
				session_.browseFile(getExcellBookURI());
			}
			//
			setMessagesOnPanel();
			//
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	class TableModelEditableList extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		ArrayList<Point> editableCells_= new ArrayList<Point>();
		public void setEditable(int row, int col) {
			//editableCells_.add(new Dimension(row, col));
			editableCells_.add(new Point(col, row));
		}
		public boolean isCellEditable(int row, int col) {
			for (int i = 0; i < editableCells_.size(); i++) {
				//if (editableCells_.get(i).width == row && editableCells_.get(i).height == col) {
				if (editableCells_.get(i).x == col && editableCells_.get(i).y == row) {
					return true;
				}
			}
			return false;
		}
		public void removeRow(int row) {
			super.removeRow(row);
			//
			ArrayList<Point> workCells= new ArrayList<Point>();
			for (int i = 0; i < editableCells_.size(); i++) {
				if (editableCells_.get(i).y != row) {
					workCells.add(editableCells_.get(i));
				}
			}
			editableCells_ = workCells;
			//
			XF310_DetailRowNumber rowNumber;
			for (int i = 0; i < editableCells_.size(); i++) {
				if (editableCells_.get(i).y > row) {
					editableCells_.get(i).y--;
					rowNumber = (XF310_DetailRowNumber)this.getValueAt(editableCells_.get(i).y, 0);
					rowNumber.setNumber(editableCells_.get(i).y + 1);
				}
			}
		}
	}

	class WorkingRow extends Object {
		private HashMap<String, Object> keyValueMap_ = new HashMap<String, Object>();
		private HashMap<String, Object> columnValueMap_ = new HashMap<String, Object>();
		private HashMap<String, Boolean> columnIsEditableMap_ = new HashMap<String, Boolean>();
		private ArrayList<Object> orderByValueList_ = new ArrayList<Object>();
		//
		public WorkingRow(HashMap<String, Object> keyValueMap, HashMap<String, Object> columnValueMap, ArrayList<Object> orderByValueList, HashMap<String, Boolean> columnIsEditableMap) {
			columnValueMap_ = columnValueMap;
			keyValueMap_ = keyValueMap;
			orderByValueList_ = orderByValueList;
			columnIsEditableMap_ = columnIsEditableMap;
		}
		public HashMap<String, Object> getKeyValueMap() {
			return keyValueMap_;
		}
		public HashMap<String, Object> getColumnValueMap() {
			return columnValueMap_;
		}
		public HashMap<String, Boolean> getColumnIsEditableMap() {
			return columnIsEditableMap_;
		}
		public ArrayList<Object> getOrderByValueList() {
			return orderByValueList_;
		}
	}

	class WorkingRowComparator implements java.util.Comparator<WorkingRow>{
		public int compare(WorkingRow row1, WorkingRow row2){
			int compareResult = 0;
			for (int i = 0; i < row1.getOrderByValueList().size(); i++) {
				compareResult = row1.getOrderByValueList().get(i).toString().compareTo(row2.getOrderByValueList().get(i).toString());
				if (detailTable.getOrderByFieldIDList().get(i).contains("(D)")) {
					compareResult = compareResult * -1;
				}
				if (compareResult != 0) {
					break;
				}
			}
			return compareResult;
		}
	}

	URI getExcellBookURI() {
		File xlsFile = null;
		String xlsFileName = "";
		FileOutputStream fileOutputStream = null;
		HSSFFont font = null;
		XF310_DetailCell cellObject = null;
		//
		HSSFWorkbook workBook = new HSSFWorkbook();
		HSSFSheet workSheet = workBook.createSheet(functionElement_.getAttribute("Name"));
		workSheet.setDefaultRowHeight( (short) 300);
		HSSFFooter workSheetFooter = workSheet.getFooter();
		workSheetFooter.setRight(functionElement_.getAttribute("Name") + "  Page " + HSSFFooter.page() + " / " + HSSFFooter.numPages() );
		//
		HSSFFont fontHeader = workBook.createFont();
		fontHeader = workBook.createFont();
		fontHeader.setFontName(res.getString("XLSFontHDR"));
		fontHeader.setFontHeightInPoints((short)11);
		//
		HSSFFont fontDataBlack = workBook.createFont();
		fontDataBlack.setFontName(res.getString("XLSFontDTL"));
		fontDataBlack.setFontHeightInPoints((short)11);
		HSSFFont fontDataRed = workBook.createFont();
		fontDataRed.setFontName(res.getString("XLSFontDTL"));
		fontDataRed.setFontHeightInPoints((short)11);
		fontDataRed.setColor(HSSFColor.RED.index);
		HSSFFont fontDataBlue = workBook.createFont();
		fontDataBlue.setFontName(res.getString("XLSFontDTL"));
		fontDataBlue.setFontHeightInPoints((short)11);
		fontDataBlue.setColor(HSSFColor.BLUE.index);
		HSSFFont fontDataGreen = workBook.createFont();
		fontDataGreen.setFontName(res.getString("XLSFontDTL"));
		fontDataGreen.setFontHeightInPoints((short)11);
		fontDataGreen.setColor(HSSFColor.GREEN.index);
		HSSFFont fontDataOrange = workBook.createFont();
		fontDataOrange.setFontName(res.getString("XLSFontDTL"));
		fontDataOrange.setFontHeightInPoints((short)11);
		fontDataOrange.setColor(HSSFColor.ORANGE.index);
		//
		HSSFCellStyle styleHeaderLabel = workBook.createCellStyle();
		styleHeaderLabel.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		styleHeaderLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleHeaderLabel.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		styleHeaderLabel.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		styleHeaderLabel.setFont(fontHeader);
		//
		HSSFCellStyle styleDetailLabel = workBook.createCellStyle();
		styleDetailLabel.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		styleDetailLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleDetailLabel.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		styleDetailLabel.setFont(fontHeader);
		//
		HSSFCellStyle styleDetailNumberLabel = workBook.createCellStyle();
		styleDetailNumberLabel.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		styleDetailNumberLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleDetailNumberLabel.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		styleDetailNumberLabel.setFont(fontHeader);
		//
		HSSFCellStyle styleDataInteger = workBook.createCellStyle();
		styleDataInteger.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleDataInteger.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleDataInteger.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleDataInteger.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleDataInteger.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		styleDataInteger.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		styleDataInteger.setFont(fontDataBlack);
		styleDataInteger.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
		//
		int currentRowNumber = -1;
		int mergeRowNumberFrom = -1;
		//
		try {
			xlsFile = session_.createTempFile(functionElement_.getAttribute("ID"), ".xls");
			xlsFileName = xlsFile.getPath();
			fileOutputStream = new FileOutputStream(xlsFileName);
			int columnIndex;
			//
			// Header Lines //
			for (int i = 0; i < headerFieldList.size() && i < 24; i++) {
				if (headerFieldList.get(i).isVisibleOnPanel()) {
					for (int j = 0; j < headerFieldList.get(i).getRows(); j++) {
						//
						currentRowNumber++;
						HSSFRow rowData = workSheet.createRow(currentRowNumber);
						//
						// Cells for header field label //
						HSSFCell cellHeader = rowData.createCell(0);
						cellHeader.setCellStyle(styleHeaderLabel);
						if (j==0) {
							mergeRowNumberFrom = currentRowNumber;
							cellHeader.setCellValue(new HSSFRichTextString(headerFieldList.get(i).getCaption()));
						}
						rowData.createCell(1).setCellStyle(styleHeaderLabel);
						//
						// Cells for header field data //
						if (headerFieldList.get(i).getColor().equals("black")) {
							font = fontDataBlack;
						}
						if (headerFieldList.get(i).getColor().equals("red")) {
							font = fontDataRed;
						}
						if (headerFieldList.get(i).getColor().equals("blue")) {
							font = fontDataBlue;
						}
						if (headerFieldList.get(i).getColor().equals("green")) {
							font = fontDataGreen;
						}
						if (headerFieldList.get(i).getColor().equals("orange")) {
							font = fontDataOrange;
						}
						setupCellAttributesForHeaderField(rowData, workBook, workSheet, headerFieldList.get(i), currentRowNumber, j, font);
					}
					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 0, 1));
					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 2, 6));
				}
			}
			//
			// Spacer //
			currentRowNumber++;
			workSheet.createRow(currentRowNumber);
			//
			// Detail Lines //
			currentRowNumber++;
			HSSFRow rowCaption = workSheet.createRow(currentRowNumber);
			for (int i = 0; i < tableModelMain.getColumnCount(); i++) {
				HSSFCell cell = rowCaption.createCell(i);
				if (i == 0) {
					cell.setCellStyle(styleDetailNumberLabel);
				} else {
					cell.setCellStyle(styleDetailLabel);
				}
				Rectangle rect = jTableMain.getCellRect(0, i, true);
				workSheet.setColumnWidth(i, rect.width * 40);
				cell.setCellValue(new HSSFRichTextString(tableModelMain.getColumnName(i)));
			}
			//
			for (int i = 0; i < tableModelMain.getRowCount(); i++) {
				currentRowNumber++;
				HSSFRow rowData = workSheet.createRow(currentRowNumber);
				//
				HSSFCell cell = rowData.createCell(0); //Column of Sequence Number
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellStyle(styleDataInteger);
				cell.setCellValue(i + 1);
				//
				columnIndex = 0;
				for (int j = 0; j < detailColumnList.size(); j++) {
					if (detailColumnList.get(j).isVisibleOnPanel()) {
						columnIndex++;
						cellObject = (XF310_DetailCell)tableModelMain.getValueAt(i,columnIndex);
						if (cellObject.getForeground().equals(Color.black)) {
							font = fontDataBlack;
						}
						if (cellObject.getForeground().equals(Color.red)) {
							font = fontDataRed;
						}
						if (cellObject.getForeground().equals(Color.blue)) {
							font = fontDataBlue;
						}
						if (cellObject.getForeground().equals(Color.green)) {
							font = fontDataGreen;
						}
						if (cellObject.getForeground().equals(Color.orange)) {
							font = fontDataOrange;
						}
						setupCellAttributesForDetailColumn(rowData.createCell(columnIndex), workBook, detailColumnList.get(j).getBasicType(), cellObject, font, detailColumnList.get(j).getDecimalSize());
					}
				}
			}
			//
			workBook.write(fileOutputStream);
			fileOutputStream.close();
			//
			messageList.add(res.getString("XLSComment1"));
			//
		} catch (Exception e1) {
			messageList.add(res.getString("XLSErrorMessage"));
			e1.printStackTrace(exceptionStream);
			try {
				fileOutputStream.close();
			} catch (Exception e2) {
				e2.printStackTrace(exceptionStream);
			}
		}
		return xlsFile.toURI();
	}

	private void setupCellAttributesForHeaderField(HSSFRow rowData, HSSFWorkbook workBook, HSSFSheet workSheet, XF310_HeaderField object, int currentRowNumber, int rowIndexInCell, HSSFFont font) {
		String wrk;
		//
		HSSFCellStyle style = workBook.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setFont(font);
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		style.setWrapText(true);
		style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		//
		HSSFCell cellValue = rowData.createCell(2);
		//
		String basicType = object.getBasicType();
		if (basicType.equals("INTEGER")) {
			wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
			if (wrk.equals("")) {
				cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
				cellValue.setCellStyle(style);
				if (rowIndexInCell==0) {
					cellValue.setCellValue(new HSSFRichTextString(""));
				}
			} else {
				cellValue.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
				style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
				cellValue.setCellStyle(style);
				if (rowIndexInCell==0) {
					cellValue.setCellValue(Double.parseDouble(wrk));
				}
			}
		} else {
			if (basicType.equals("FLOAT")) {
				wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
				if (wrk.equals("")) {
					cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(new HSSFRichTextString(""));
					}
				} else {
					cellValue.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
					style.setDataFormat(XFUtility.getFloatFormat(workBook, object.getDecimalSize()));
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(Double.parseDouble(wrk));
					}
				}
			} else {
				cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
				cellValue.setCellStyle(style);
				if (rowIndexInCell==0) {
					if (basicType.equals("STRING")) {
						if (object.isImage()) {
							style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
							style.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);
							style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
							HSSFPatriarch patriarch = workSheet.createDrawingPatriarch();
							HSSFClientAnchor anchor = null;
							//HSSFPicture picture = null;
							cellValue.setCellStyle(style);
							cellValue.setCellValue(new HSSFRichTextString((String)object.getInternalValue()));
							int imageType = -1;
							String fileName = (String)object.getExternalValue();
							File imageFile = new File(fileName);
							if (imageFile.exists()) {
								boolean isValidFileType = false;
								if (fileName.contains(".png") || fileName.contains(".PNG")) {
									imageType = HSSFWorkbook.PICTURE_TYPE_PNG;
									isValidFileType = true;
								}
								if (fileName.contains(".jpg") || fileName.contains(".JPG") || fileName.contains(".jpeg") || fileName.contains(".JPEG")) {
									imageType = HSSFWorkbook.PICTURE_TYPE_JPEG;
									isValidFileType = true;
								}
								if (isValidFileType) {
									int pictureIndex;
									FileInputStream fis = null;
									ByteArrayOutputStream bos = null;
									try {
										// read in the image file //
										fis = new FileInputStream(imageFile);
										bos = new ByteArrayOutputStream( );
										int c;
										// copy the image bytes into the ByteArrayOutputStream //
										while ((c = fis.read()) != -1) {
											bos.write(c);
										}
										// add the image bytes to the workbook //
										pictureIndex = workBook.addPicture( bos.toByteArray(), imageType);
										anchor = new HSSFClientAnchor(0,0,0,0,(short)2,currentRowNumber,(short)6,currentRowNumber + object.getRows());
										anchor.setAnchorType(0);
										anchor.setDx1(30);
										anchor.setDy1(30);
										anchor.setDx2(-30);
										anchor.setDy2(-250);
										//picture = patriarch.createPicture(anchor, pictureIndex);
										patriarch.createPicture(anchor, pictureIndex);
										//
									} catch(Exception e) {
										e.printStackTrace(exceptionStream);
									} finally {
										try {
											if (fis != null) {
												fis.close();
											}
											if (bos != null) {
												bos.close();
											}
										} catch (IOException e) {
											e.printStackTrace(exceptionStream);
										}
									}
								}
							}
						} else {
							cellValue.setCellStyle(style);
							cellValue.setCellValue(new HSSFRichTextString((String)object.getExternalValue()));
						}
					} else {
						if (basicType.equals("DATE")) {
							java.util.Date utilDate = XFUtility.convertDateFromStringToUtil((String)object.getInternalValue());
							String text = XFUtility.getUserExpressionOfUtilDate(utilDate, session_.getDateFormat(), false);
							cellValue.setCellValue(new HSSFRichTextString(text));
						}
						if (object.getBasicType().equals("DATETIME") || object.getBasicType().equals("TIME")) {
							cellValue.setCellValue(new HSSFRichTextString(object.getInternalValue().toString()));
						}
					}
				}
			}
		}
		rowData.createCell(3).setCellStyle(style);
		rowData.createCell(4).setCellStyle(style);
		rowData.createCell(5).setCellStyle(style);
		rowData.createCell(6).setCellStyle(style);
	}

	private void setupCellAttributesForDetailColumn(HSSFCell cell, HSSFWorkbook workBook, String basicType, XF310_DetailCell object, HSSFFont font, int decimalSize) {
		String wrk;
		//
		HSSFCellStyle style = workBook.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setFont(font);
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		style.setWrapText(true);
		style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		//
		Object value = object.getExternalValue();
		if (basicType.equals("INTEGER")) {
			if (value == null) {
				wrk = "";
			} else {
				wrk = XFUtility.getStringNumber(value.toString());
			}
			if (wrk.equals("")) {
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellStyle(style);
				cell.setCellValue(new HSSFRichTextString(wrk));
			} else {
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
				style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
				cell.setCellStyle(style);
				cell.setCellValue(Double.parseDouble(wrk));
			}
		} else {
			if (basicType.equals("FLOAT")) {
				if (value == null) {
					wrk = "";
				} else {
					wrk = XFUtility.getStringNumber(value.toString());
				}
				if (wrk.equals("")) {
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					cell.setCellStyle(style);
					cell.setCellValue(new HSSFRichTextString(wrk));
				} else {
					cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
					//style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.000"));
					style.setDataFormat(XFUtility.getFloatFormat(workBook, decimalSize));
					cell.setCellStyle(style);
					cell.setCellValue(Double.parseDouble(wrk));
				}
			} else {
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellStyle(style);
				if (value == null) {
					wrk = "";
				} else {
					wrk = value.toString();
				}
				cell.setCellValue(new HSSFRichTextString(wrk));
			}
		}
	}
	
	void jTableMain_focusGained(FocusEvent e) {
		if (jTableMain.getRowCount() > 0) {
			jTableMain.setSelectionBackground(selectionColorWithFocus);
			jTableMain.setSelectionForeground(Color.white);
		}
	}

	void jTableMain_focusLost(FocusEvent e) {
		jTableMain.setSelectionBackground(selectionColorWithoutFocus);
		jTableMain.setSelectionForeground(Color.black);
	}

	void jScrollPaneTable_mousePressed(MouseEvent e) {
		jTableMain.requestFocus();
	}

	void setMessagesOnPanel() {
		//
		XF310_DetailCell cell;
		int workRow = 0;
		boolean topErrorFieldNotFound = true;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (headerFieldList.get(i).isError()) {
				if (topErrorFieldNotFound) {
					headerFieldList.get(i).requestFocus();
					topErrorFieldNotFound = false;
					//break;
				}
				messageList.add(workRow, headerFieldList.get(i).getCaption() + res.getString("Colon") + headerFieldList.get(i).getError());
				workRow++;
			}
		}
		if (topErrorFieldNotFound) {
			for (int i = 0; i < jTableMain.getRowCount(); i++) {
				int rowNumber = jTableMain.convertRowIndexToModel(i);
				if (rowNumber > -1) {
					for (int j = 1; j < jTableMain.getColumnCount(); j++) {
						cell = (XF310_DetailCell)tableModelMain.getValueAt(rowNumber, j);
						if (cell.isError()) {
							jTableMain.changeSelection(rowNumber, j, false, false);
							topErrorFieldNotFound = false;
							break;
						}
					}
				}
			}
		}
		//
		jTextAreaMessages.setText("");
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < messageList.size(); i++) {
			if (i > 0) {
				sb.append("\n");
			}
			if (messageList.size() > 1) {
				sb.append("(" + Integer.toString(i+1) + "/"+ Integer.toString(messageList.size()) + ") " + messageList.get(i));
			} else {
				sb.append(messageList.get(i));
			}
		}
		jTextAreaMessages.setText(sb.toString());
		//
		int heightOfErrorMessages = (messageList.size() + 1) * 20;
		if (heightOfErrorMessages <= 40) {
			jSplitPaneMain.setDividerLocation(this.getHeight() - 40 - 80);
		}
		if (heightOfErrorMessages > 40 && heightOfErrorMessages <= 240) {
			jSplitPaneMain.setDividerLocation(this.getHeight() - heightOfErrorMessages - 80);
		}
		if (heightOfErrorMessages > 240) {
			jSplitPaneMain.setDividerLocation(this.getHeight() - 240 - 80);
		}
	}
	
	void setActiveCellEditor(XFTableCellEditor editor) {
		activeCellEditor = editor;
	}

	public String getFunctionID() {
		return functionElement_.getAttribute("ID");
	}
	
	public ArrayList<String> getMessageList() {
		return messageList;
	}

	public String getScriptNameRunning() {
		return scriptNameRunning;
	}

	public String getAddRowListTitle() {
		return addRowListTitle;
	}

	public org.w3c.dom.Element getFunctionElement() {
		return functionElement_;
	}

	public ArrayList<String> getKeyFieldList() {
		return headerTable.getKeyFieldIDList();
	}

	public Session getSession() {
		return session_;
	}

	//public StringBuffer getProcessLog() {
	//	return processLog;
	//}
	
	public String getFunctionInfo() {
		return jLabelFunctionID.getText();
	}

	public PrintStream getExceptionStream() {
		return exceptionStream;
	}

	public void setExceptionHeader(String value) {
		exceptionHeader = value;
	}
	
	public ScriptEngine getScriptEngine() {
		return scriptEngine;
	}

	public Bindings getEngineScriptBindings() {
		return 	engineScriptBindings;
	}

	public void evalScript(String scriptName, String scriptText, Bindings bindings) throws ScriptException {
		if (!scriptText.equals("")) {
			scriptNameRunning = scriptName;
			if (bindings == null) {
				scriptEngine.eval(scriptText);
			} else {
				scriptEngine.eval(scriptText, bindings);
				
			}
		}
	}

	public HashMap<String, Object> getParmMap() {
		return parmMap_;
	}
	
	public void setProcessLog(String text) {
		XFUtility.appendLog(text, processLog);
	}

	public HashMap<String, Object> getReturnMap() {
		return returnMap_;
	}

	public void setReturnMap(HashMap<String, Object> map) {
		returnMap_ = map;
	}
	
	public XF310_HeaderTable getHeaderTable() {
		return headerTable;
	}
	
	public XF310_HeaderField getFirstEditableHeaderField() {
		return firstEditableHeaderField;
	}
	
	public void setFirstEditableHeaderField(XF310_HeaderField field) {
		firstEditableHeaderField = field;
	}
	
	public ArrayList<XF310_HeaderReferTable> getHeaderReferTableList() {
		return headerReferTableList;
	}

	public ArrayList<XF310_HeaderField> getHeaderFieldList() {
		return headerFieldList;
	}

	public XF310_HeaderField getHeaderFieldObjectByID(String tableID, String tableAlias, String fieldID) {
		XF310_HeaderField headerField = null;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (tableID.equals("")) {
				if (headerFieldList.get(i).getTableAlias().equals(tableAlias) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					headerField = headerFieldList.get(i);
					break;
				}
			}
			if (tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					headerField = headerFieldList.get(i);
					break;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID) && headerFieldList.get(i).getTableAlias().equals(tableAlias) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					headerField = headerFieldList.get(i);
					break;
				}
			}
		}
		return headerField;
	}

	public boolean containsHeaderField(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (tableID.equals("")) {
				if (headerFieldList.get(i).getTableAlias().equals(tableAlias)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID) && headerFieldList.get(i).getTableAlias().equals(tableAlias) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public boolean containsDetailField(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < detailColumnList.size(); i++) {
			if (tableID.equals("")) {
				if (detailColumnList.get(i).getTableAlias().equals(tableAlias)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID) && detailColumnList.get(i).getTableAlias().equals(tableAlias) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public XF310_DetailTable getDetailTable() {
		return detailTable;
	}

	public TableModelEditableList getTableModel() {
		return tableModelMain;
	}

	public JTable getJTableMain() {
		return jTableMain;
	}

	public ArrayList<XF310_DetailColumn> getDetailColumnList() {
		return detailColumnList;
	}

	public ArrayList<XF310_DetailReferTable> getDetailReferTableList() {
		return detailReferTableList;
	}

	public XF310_DetailColumn getDetailColumnObjectByID(String tableID, String tableAlias, String fieldID) {
		XF310_DetailColumn detailColumnField = null;
		for (int i = 0; i < detailColumnList.size(); i++) {
			if (tableID.equals("")) {
				if (detailColumnList.get(i).getTableAlias().equals(tableAlias) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnList.get(i);
					break;
				}
			}
			if (tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnList.get(i);
					break;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID) && detailColumnList.get(i).getTableAlias().equals(tableAlias) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnList.get(i);
					break;
				}
			}
		}
		return detailColumnField;
	}

	public String getTableIDOfTableAlias(String tableAlias) {
		String tableID = tableAlias;
		org.w3c.dom.Element workElement;
		//
		for (int j = 0; j < headerReferElementList.getLength(); j++) {
			workElement = (org.w3c.dom.Element)headerReferElementList.item(j);
			if (workElement.getAttribute("TableAlias").equals(tableAlias)) {
				tableID = workElement.getAttribute("ToTable");
				break;
			}
		}
		//
		if (detailReferElementList != null) {
			for (int j = 0; j < detailReferElementList.getLength(); j++) {
				workElement = (org.w3c.dom.Element)detailReferElementList.item(j);
				if (workElement.getAttribute("TableAlias").equals(tableAlias)) {
					tableID = workElement.getAttribute("ToTable");
					break;
				}
			}
		}
		//
		return tableID;
	}

	public Object getValueOfHeaderFieldByName(String dataSourceName) {
		Object obj = null;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (headerFieldList.get(i).getDataSourceName().equals(dataSourceName)) {
				obj = headerFieldList.get(i).getInternalValue();
				break;
			}
		}
		return obj;
	}
}

class XF310_HeaderField extends JPanel implements XFScriptableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	org.w3c.dom.Element functionFieldElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private String tableID_ = "";
	private String tableAlias_ = "";
	private String fieldID_ = "";
	private String dataType = "";
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private String fieldCaption = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private String fieldOptions = "";
	private ArrayList<String> fieldOptionList;
	private int fieldRows = 1;
	private JPanel jPanelField = new JPanel();
	private JLabel jLabelField = new JLabel();
	private JLabel jLabelFieldComment = new JLabel();
	private XF310_HeaderComboBox xFComboBox = null;
	private XF310_HeaderPromptCall xFPromptCall = null;
	private XFTextField xFTextField = null;
	private XFDateField xFDateField = null;
	private XFTextArea xFTextArea = null;
	private XFYMonthBox xFYMonthBox = null;
	private XFMSeqBox xFMSeqBox = null;
	private XFFYearBox xFFYearBox = null;
	private XFUrlField xFUrlField = null;
	private XFImageField xFImageField = null;
	private XFEditableField component = null;
	private boolean isKey = false;
	private boolean isNullable = true;
	private boolean isFieldOnPrimaryTable = false;
	private boolean isVisibleOnPanel = true;
	private boolean isEditable = true;
	private boolean isHorizontal = false;
	private boolean isVirtualField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private boolean isImage = false;
	private boolean isError = false;
	private String errorMessage = "";
	private org.w3c.dom.Element promptFunctionElement_ = null;
	private int positionMargin = 0;
	private Color foreground = Color.black;
	private XF310 dialog_;

	public XF310_HeaderField(org.w3c.dom.Element functionFieldElement, XF310 dialog){
		super();
		//
		String wrkStr;
		dialog_ = dialog;
		functionFieldElement_ = functionFieldElement;
		fieldOptions = functionFieldElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);
		//
		StringTokenizer workTokenizer = new StringTokenizer(functionFieldElement_.getAttribute("DataSource"), "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog.getTableIDOfTableAlias(tableAlias_);
		fieldID_ =workTokenizer.nextToken();
		//
		if (tableID_.equals(dialog_.getHeaderTable().getTableID()) && tableID_.equals(tableAlias_)) {
			isFieldOnPrimaryTable = true;
			//
			ArrayList<String> keyFieldList = dialog_.getHeaderTable().getKeyFieldIDList();
			for (int i = 0; i < keyFieldList.size(); i++) {
				if (keyFieldList.get(i).equals(fieldID_)) {
					isKey = true;
					isEditable = false;
					break;
				}
			}
		} else {
			isEditable = false;
		}
		//
		if (fieldOptionList.contains("HORIZONTAL")) {
			isHorizontal = true;
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "HORIZONTAL");
		if (!wrkStr.equals("")) {
			isHorizontal = true;
			positionMargin = Integer.parseInt(wrkStr);
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "VERTICAL");
		if (!wrkStr.equals("")) {
			positionMargin = Integer.parseInt(wrkStr);
		}
		//
		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID_ + "." + fieldID_ + res.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		//if (dataSize > 50) {
		//	dataSize = 50;
		//}
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}
		//
		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = wrkStr;
		}
		jLabelField.setText(fieldCaption);
		jLabelField.setPreferredSize(new Dimension(120, 24));
		jLabelField.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelField.setVerticalAlignment(SwingConstants.TOP);
		FontMetrics metrics = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		if (metrics.stringWidth(fieldCaption) > 120) {
			jLabelField.setFont(new java.awt.Font("Dialog", 0, 12));
			metrics = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 12));
			if (metrics.stringWidth(fieldCaption) > 120) {
				jLabelField.setFont(new java.awt.Font("Dialog", 0, 10));
			} else {
				jLabelField.setFont(new java.awt.Font("Dialog", 0, 12));
			}
		} else {
			jLabelField.setFont(new java.awt.Font("Dialog", 0, 14));
		}
		//
		if (fieldOptionList.contains("PROMPT_LIST")) {
			isEditable = true;
			//
			XF310_HeaderReferTable referTable = null;
			ArrayList<XF310_HeaderReferTable> referTableList = dialog_.getHeaderReferTableList();
			for (int i = 0; i < referTableList.size(); i++) {
				if (referTableList.get(i).getTableID().equals(tableID_)) {
					if (referTableList.get(i).getTableAlias().equals("") || referTableList.get(i).getTableAlias().equals(tableAlias_)) {
						referTable = referTableList.get(i);
						break;
					}
				}
			}
			//
			xFComboBox = new XF310_HeaderComboBox(functionFieldElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, referTable, isNullable);
			xFComboBox.setLocation(5, 0);
			component = xFComboBox;
		} else {
			wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
			if (!wrkStr.equals("")) {
				isEditable = true;
				NodeList functionList = dialog_.getSession().getFunctionList();
				for (int k = 0; k < functionList.getLength(); k++) {
					workElement = (org.w3c.dom.Element)functionList.item(k);
					if (workElement.getAttribute("ID").equals(wrkStr)) {
						promptFunctionElement_ = workElement;
						break;
					}
				}
				//
				xFPromptCall = new XF310_HeaderPromptCall(functionFieldElement_, promptFunctionElement_, dialog_);
				xFPromptCall.setLocation(5, 0);
				if (this.isFieldOnPrimaryTable) {
					xFPromptCall.setEditable(true);
				}
				component = xFPromptCall;
			} else {
				if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN").equals("") || !XFUtility.getOptionValueWithKeyword(dataTypeOptions, "VALUES").equals("")) {
					xFComboBox = new XF310_HeaderComboBox(functionFieldElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, null, isNullable);
					xFComboBox.setLocation(5, 0);
					component = xFComboBox;
				} else {
					if (dataType.equals("VARCHAR") || dataType.equals("LONG VARCHAR")) {
						xFTextArea = new XFTextArea(dataSize, "", fieldOptions);
						xFTextArea.setLocation(5, 0);
						xFTextArea.setEditable(false);
						component = xFTextArea;
					} else {
						if (dataTypeOptionList.contains("URL")) {
							xFUrlField = new XFUrlField(dataSize);
							xFUrlField.setLocation(5, 0);
							xFUrlField.setEditable(false);
							component = xFUrlField;
						} else {
							if (dataTypeOptionList.contains("IMAGE")) {
								xFImageField = new XFImageField(fieldOptions, dataSize, dialog_.getSession().getImageFileFolder());
								xFImageField.setLocation(5, 0);
								component = xFImageField;
								isImage = true;
							} else {
								if (dataType.equals("DATE")) {
									xFDateField = new XFDateField(dialog_.getSession());
									xFDateField.setLocation(5, 0);
									xFDateField.setEditable(false);
									component = xFDateField;
								} else {
									if (dataTypeOptionList.contains("YMONTH")) {
										xFYMonthBox = new XFYMonthBox(dialog_.getSession());
										xFYMonthBox.setLocation(5, 0);
										xFYMonthBox.setEditable(false);
										component = xFYMonthBox;
									} else {
										if (dataTypeOptionList.contains("MSEQ")) {
											xFMSeqBox = new XFMSeqBox(dialog_.getSession());
											xFMSeqBox.setLocation(5, 0);
											xFMSeqBox.setEditable(false);
											component = xFMSeqBox;
										} else {
											if (dataTypeOptionList.contains("FYEAR")) {
												xFFYearBox = new XFFYearBox(dialog_.getSession());
												xFFYearBox.setLocation(5, 0);
												xFFYearBox.setEditable(false);
												component = xFFYearBox;
											} else {
												xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions);
												xFTextField.setLocation(5, 0);
												xFTextField.setEditable(false);
												component = xFTextField;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
		if ((isFieldOnPrimaryTable && isEditable)
				|| (!isFieldOnPrimaryTable && fieldOptionList.contains("PROMPT_LIST"))
				|| (!isFieldOnPrimaryTable && !wrkStr.equals(""))) {
			component.setEditable(true);
			if (dialog_.getFirstEditableHeaderField() == null) {
				dialog_.setFirstEditableHeaderField(this);
			}
		} else {
			component.setEditable(false);
		}
		//
		fieldRows = component.getRows();
		//
		jPanelField.setLayout(null);
		jPanelField.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		jPanelField.add((JComponent)component);
		//
		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		if (fieldOptionList.contains("NO_CAPTION")) {
			this.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		} else {
			this.setPreferredSize(new Dimension(component.getWidth() + 130, component.getHeight()));
			this.add(jLabelField, BorderLayout.WEST);
		}
		this.add(jPanelField, BorderLayout.CENTER);
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "COMMENT");
		if (!wrkStr.equals("")) {
			jLabelFieldComment.setText(wrkStr);
			jLabelFieldComment.setForeground(Color.gray);
			jLabelFieldComment.setFont(new java.awt.Font("Dialog", 0, 12));
			jLabelFieldComment.setVerticalAlignment(SwingConstants.TOP);
			metrics = jLabelFieldComment.getFontMetrics(new java.awt.Font("Dialog", 0, 12));
			this.setPreferredSize(new Dimension(this.getPreferredSize().width + metrics.stringWidth(wrkStr), this.getPreferredSize().height));
			this.add(jLabelFieldComment, BorderLayout.EAST);
		}
		//
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
		this.setFocusable(true);
		this.addFocusListener(new ComponentFocusListener());
	}

	public XF310_HeaderField(String tableID, String tableAlias, String fieldID, XF310 dialog){
		super();
		//
		dialog_ = dialog;
		functionFieldElement_ = null;
		fieldOptions = "";
		fieldOptionList = new ArrayList<String>();
		//
		isVisibleOnPanel = false;
		//
		tableID_ = tableID;
		fieldID_ = fieldID;
		dialog_ = dialog;
		if (tableAlias.equals("")) {
			tableAlias_ = tableID;
		} else {
			tableAlias_ = tableAlias;
		}
		//
		if (tableID_.equals(dialog_.getHeaderTable().getTableID()) && tableID_.equals(tableAlias_)) {
			isFieldOnPrimaryTable = true;
			//
			ArrayList<String> keyFieldList = dialog_.getHeaderTable().getKeyFieldIDList();
			for (int i = 0; i < keyFieldList.size(); i++) {
				if (keyFieldList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
		}
		//
		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID_ + "." + fieldID_ + res.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		//if (dataSize > 50) {
		//	dataSize = 50;
		//}
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}
		//
		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			StringTokenizer workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}
		//
		xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions);
		xFTextField.setLocation(5, 0);
		xFTextField.setEditable(false);
		//
		component = xFTextField;
		//
		fieldRows = component.getRows();
		jPanelField.setLayout(null);
		jPanelField.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		jPanelField.add((JComponent)component);
		//
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(component.getWidth() + 130, component.getHeight()));
		this.add(jPanelField, BorderLayout.CENTER);
		//
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
	}

	public void checkPromptKeyEdit(){
		if (!XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL").equals("")) {
			if (xFPromptCall.hasEditControlledKey()) {
				this.setEditable(false);
			}
		}
	}

	public boolean isNull(){
		return XFUtility.isNullValue(this.getBasicType(), component.getInternalValue());
	}

	public String getTableID(){
		return tableID_;
	}

	public String getTableAlias(){
		return tableAlias_;
	}

	public String getFieldID(){
		return fieldID_;
	}

	public String getDataSourceName(){
		return tableAlias_ + "." + fieldID_;
	}

	public int getDecimalSize(){
		return decimalSize;
	}

	public String getCaption(){
		return fieldCaption;
	}

	public boolean isVisibleOnPanel(){
		return isVisibleOnPanel;
	}

	public boolean isEditable(){
		return isEditable;
	}

	public void setEditable(boolean editable){
		isEditable = editable;
		component.setEditable(editable);
	}

	public boolean isHorizontal(){
		return isHorizontal;
	}

	public int getPositionMargin(){
		return positionMargin;
	}

	public boolean isNullable(){
		return isNullable;
	}

	public boolean isVirtualField(){
		return isVirtualField;
	}

	public boolean isRangeKeyFieldValid(){
		return isRangeKeyFieldValid;
	}

	public boolean isRangeKeyFieldExpire(){
		return isRangeKeyFieldExpire;
	}

	public org.w3c.dom.Element getTableElement(){
		return tableElement;
	}

	public boolean isImage(){
		return isImage;
	}

	public int getRows(){
		return fieldRows;
	}

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
	}

	public boolean isFieldOnPrimaryTable(){
		return isFieldOnPrimaryTable;
	}

	public boolean isKey(){
		return isKey;
	}

	public boolean isNullError(){
		String basicType = this.getBasicType();
		boolean isNullError = false;
		//
		if (basicType.equals("INTEGER")) {
			int value = Integer.parseInt((String)component.getInternalValue());
			if (!this.isNullable) {
				if (value == 0) {
					isNullError = true;
				}
			}
		}
		if (basicType.equals("FLOAT")) {
			double value = Double.parseDouble((String)component.getInternalValue());
			if (!this.isNullable) {
				if (value == 0) {
					isNullError = true;
				}
			}
		}
		if (basicType.equals("DATE")) {
			String strDate = (String)component.getInternalValue();
			if (!this.isNullable) {
				if (strDate == null || strDate.equals("")) {
					isNullError = true;
				}
			}
		}
		if (basicType.equals("STRING")) {
			String strWrk = (String)component.getInternalValue();
			if (!this.isNullable) {
				if (strWrk.equals("")) {
					isNullError = true;
				}
			}
			if (dataTypeOptionList.contains("YMONTH") && strWrk.length() > 0 && strWrk.length() < 6) {
				isNullError = true;
			}
			if (dataTypeOptionList.contains("FYEAR") && strWrk.length() > 0 && strWrk.length() < 4) {
				isNullError = true;
			}
		}
		//
		if (isNullError) { 
			this.setError(res.getString("FunctionError16"));
		}
		//
		return isNullError;
	}

	public void setValueOfResultSet(ResultSet result){
		try {
			if (this.isVirtualField) {
				if (this.isRangeKeyFieldExpire()) {
					component.setValue(XFUtility.calculateExpireValue(this.getTableElement(), result, dialog_.getSession()));
				}
			} else {
				//
				Object value = result.getObject(this.getFieldID()); 
				//
				if (this.getBasicType().equals("INTEGER")) {
					if (value == null || value.equals("")) {
						component.setValue("");
					} else {
						String wrkStr = value.toString();
						int pos = wrkStr.indexOf(".");
						if (pos >= 0) {
							wrkStr = wrkStr.substring(0, pos);
						}
						component.setValue(Integer.parseInt(wrkStr));
					}
				} else {
					if (this.getBasicType().equals("FLOAT")) {
						if (value == null || value.equals("")) {
							component.setValue("");
						} else {
							component.setValue(Float.parseFloat(value.toString()));
						}
					} else {
						if (result == null) {
							component.setValue("");
						} else {
							String stringValue = result.getString(this.getFieldID());
							if (stringValue == null) {
								component.setValue("");
							} else {
								component.setValue(stringValue);
							}
						}
					}
				}
			}
			//
			component.setOldValue(component.getInternalValue());
			//
		} catch (SQLException e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	public void setValue(Object object){
		XFUtility.setValueToEditableField(this.getBasicType(), object, component);
	}

	public Object getInternalValue(){
		Object returnObj = null;
		//
		returnObj = (String)component.getInternalValue();
		//
		return returnObj;
	}

	public void setOldValue(Object object){
		XFUtility.setOldValueToEditableField(this.getBasicType(), object, component);
	}

	public Object getOldValue(){
		Object returnObj = null;
		//
		if (this.getBasicType().equals("INTEGER")) {
			returnObj = Integer.parseInt((String)component.getOldValue());
		} else {
			if (this.getBasicType().equals("FLOAT")) {
				returnObj = Float.parseFloat((String)component.getOldValue());
			} else {
				if (component.getOldValue() == null) {
					returnObj = "";
				} else {
					returnObj = (String)component.getOldValue();
				}
			}
		}
		//
		return returnObj;
	}

	public Object getValueForScript(){
		Object returnObj = null;
		//
		if (this.getBasicType().equals("INTEGER")) {
			returnObj = Integer.parseInt((String)component.getInternalValue());
		} else {
			if (this.getBasicType().equals("FLOAT")) {
				returnObj = Float.parseFloat((String)component.getInternalValue());
			} else {
				returnObj = (String)component.getInternalValue();
			}
		}
		//
		return returnObj;
	}

	public Object getExternalValue(){
		Object returnObj = (String)component.getExternalValue();
		return returnObj;
	}

	public Object getNullValue(){
		return XFUtility.getNullValueOfBasicType(this.getBasicType());
	}

	public Object getValue() {
		Object returnObj = null;
		//
		if (this.getBasicType().equals("INTEGER")) {
			returnObj = Integer.parseInt((String)component.getInternalValue());
		} else {
			if (this.getBasicType().equals("FLOAT")) {
				returnObj = Float.parseFloat((String)component.getInternalValue());
			} else {
				if (component.getInternalValue() == null) {
					returnObj = "";
				} else {
					returnObj = (String)component.getInternalValue();
				}
			}
		}
		//
		return returnObj;
	}

	public void setError(boolean error) {
		if (error) {
			isError = true;
			if (component.isEditable()) {
				component.setBackground(XFUtility.ERROR_COLOR);
			}
		} else {
			isError = false;
			this.errorMessage = "";
			if (component.isEditable()) {
				component.setBackground(XFUtility.ACTIVE_COLOR);
			} else {
				component.setBackground(XFUtility.INACTIVE_COLOR);
			}
		}
	}

	public boolean isError() {
		return isError;
	}

	public void setError(String message) {
		if (!message.equals("") && this.errorMessage.equals("")) {
			setError(true);
			this.errorMessage = message;
		}
	}

	public String getError() {
		return errorMessage;
	}

	public void setColor(String color) {
		foreground = XFUtility.convertStringToColor(color);
		component.setForeground(foreground);
	}

	public String getColor() {
		return XFUtility.convertColorToString(foreground);
	}

	public String getFieldIDInScript(){
		return tableAlias_ + "_" + fieldID_;
	}

	class ComponentFocusListener implements FocusListener{
		public void focusLost(FocusEvent event){
		}
		public void focusGained(FocusEvent event){
			if (isEditable) {
				component.requestFocus();
			} else {
				transferFocus();
			}
		}
	}
}

class XF310_DetailCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
		//
		XF310_DetailCell cell = (XF310_DetailCell)value;
		//
		setText(cell.getExternalValue().toString());
		setFont(new java.awt.Font("Monospaced", 0, 14));
		if (column == 0) {
			setFocusable(false);
		}
		//
		if (cell.isEditable()) {
			if (row%2==0) {
				if (cell.isError()) {
					setBackground(XFUtility.ERROR_COLOR);
				} else {
					setBackground(table.getBackground());
				}
			} else {
				if (cell.isError()) {
					setBackground(XFUtility.ERROR_COLOR);
				} else {
					setBackground(XFUtility.ODD_ROW_COLOR);
				}
			}
			setForeground(cell.getForeground());
		} else {
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setBackground(SystemColor.control);
				setForeground(cell.getForeground());
			}
			setFocusable(false);
		}
		//
		validate();
		return this;
	}
}

class XF310_DetailRowNumberRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
		XF310_DetailRowNumber cell = (XF310_DetailRowNumber)value;
		//
		setText(cell.toString());
		setFont(new java.awt.Font("SansSerif", 0, 14));
		setHorizontalAlignment(SwingConstants.RIGHT);
		//
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setBackground(SystemColor.control);
			setForeground(table.getForeground());
		}
		//
		validate();
		return this;
	}
}

class XF310_DetailCellEditorWithTextField extends XFTextField implements XFTableCellEditor {
	private static final long serialVersionUID = 1L;
	protected EventListenerList listenerList = new EventListenerList();
	protected ChangeEvent changeEvent = new ChangeEvent(this);
	private XF310_DetailCell detailCell = null;
	private XF310 dialog_ = null;

	public XF310_DetailCellEditorWithTextField(XF310_DetailColumn detailColumn, XF310 dialog) {
		super(detailColumn.getBasicType(), detailColumn.getDataSize(), detailColumn.getDecimalSize(), detailColumn.getDataTypeOptions(), detailColumn.getFieldOptions());
		//
		dialog_ = dialog;
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setEditable(true);
		if (detailColumn.getBasicType().equals("INTEGER") || detailColumn.getBasicType().equals("FLOAT")) {
			this.setHorizontalAlignment(SwingConstants.RIGHT);
		} else {
			this.setHorizontalAlignment(SwingConstants.LEFT);
		}
		//
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				fireEditingStopped();
			} 
		});
		this.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				setActiveCellEditor();
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					stopCellEditing();
					dialog_.getMessageList().clear();
					dialog_.checkErrorsToUpdate(true);
					dialog_.setMessagesOnPanel();
				}
				if (e.getKeyCode() == dialog_.getKeyCodeToUpdate()) {
					stopCellEditing();
					dialog_.getMessageList().clear();
					dialog_.checkErrorsToUpdate(false);
					dialog_.setMessagesOnPanel();
				}
			}
		});
	}
	
	void setActiveCellEditor() {
		dialog_.setActiveCellEditor(this);
	}

	public void addCellEditorListener(CellEditorListener listener) {
		listenerList.add(CellEditorListener.class, listener);
	} 

	public void removeCellEditorListener(CellEditorListener listener) {
		listenerList.remove(CellEditorListener.class, listener);
	} 

	protected void fireEditingStopped() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingStopped(changeEvent);
			}
		} 
	} 

	protected void fireEditingCanceled() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingCanceled(changeEvent);
			} 
		} 
	} 

	public void cancelCellEditing() {
	    fireEditingCanceled();
	} 

	public boolean stopCellEditing() {
		detailCell.setInternalValue(this.getText());
		fireEditingStopped();
		return true;
	} 

	public boolean isCellEditable(EventObject event) {
		return true;
	} 

	public boolean shouldSelectCell(EventObject event) {
		return true;
	} 

	public Object getCellEditorValue() {
		return detailCell;
	} 

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
		detailCell = (XF310_DetailCell)value;
		if (detailCell.isError()) {
			this.setBackground(XFUtility.ERROR_COLOR);
		} else {
			this.setBackground(XFUtility.ACTIVE_COLOR);
		}
		this.setValue(detailCell.getExternalValue());
		return this;
	}
}            

class XF310_DetailCellEditorWithDateField extends XFDateField implements XFTableCellEditor {
	private static final long serialVersionUID = 1L;
	protected EventListenerList listenerList = new EventListenerList();
	protected ChangeEvent changeEvent = new ChangeEvent(this);
	private XF310_DetailCell detailCell = null;
	private XF310 dialog_ = null;

	public XF310_DetailCellEditorWithDateField(XF310 dialog) {
		super(dialog.getSession());
		dialog_ = dialog;
		//
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setEditable(true);
		this.setFont(new java.awt.Font("Monospaced", 0, 12));
		this.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				setActiveCellEditor();
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					stopCellEditing();
					dialog_.getMessageList().clear();
					dialog_.checkErrorsToUpdate(true);
					dialog_.setMessagesOnPanel();
				}
				if (e.getKeyCode() == dialog_.getKeyCodeToUpdate()) {
					stopCellEditing();
					dialog_.getMessageList().clear();
					dialog_.checkErrorsToUpdate(false);
					dialog_.setMessagesOnPanel();
				}
			}
		});
	}
	
	void setActiveCellEditor() {
		dialog_.setActiveCellEditor(this);
	}

	void jButton_actionPerformed(ActionEvent e) {
		//java.util.Date selectedValue = dialog_.getSession().getDateOnCalendar((Component)this, this.getDate());
		java.util.Date selectedValue = super.getDateOnCalendar(this.getDate());
		this.setUtilDateValue(selectedValue);
		stopCellEditing();
	}

	public void addCellEditorListener(CellEditorListener listener) {
		listenerList.add(CellEditorListener.class, listener);
	} 

	public void removeCellEditorListener(CellEditorListener listener) {
		listenerList.remove(CellEditorListener.class, listener);
	} 

	protected void fireEditingStopped() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingStopped(changeEvent);
			}
		} 
	} 

	protected void fireEditingCanceled() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingCanceled(changeEvent);
			} 
		} 
	} 

	public void cancelCellEditing() {
	    fireEditingCanceled();
	} 

	public boolean stopCellEditing() {
		detailCell.setInternalValue(this.getInternalValue());
		fireEditingStopped();
		return true;
	} 

	public boolean isCellEditable(EventObject event) {
		return true;
	} 

	public boolean shouldSelectCell(EventObject event) {
		return true;
	} 

	public Object getCellEditorValue() {
		return detailCell;
	} 

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
		detailCell = (XF310_DetailCell)value;
		if (detailCell.isError()) {
			this.setBackground(XFUtility.ERROR_COLOR);
		} else {
			this.setBackground(XFUtility.ACTIVE_COLOR);
		}
		this.setValue((String)detailCell.getInternalValue());
		return this;
	}
}            

class XF310_DetailCellEditorWithYMonthBox extends JPanel implements XFTableCellEditor {
	private static final long serialVersionUID = 1L;
	private JComboBox jComboBoxYear = new JComboBox();
	private JComboBox jComboBoxMonth = new JComboBox();
	protected EventListenerList listenerList = new EventListenerList();
	protected ChangeEvent changeEvent = new ChangeEvent(this);
	private XF310_DetailCell detailCell = null;
	private XF310 dialog_ = null;

	public XF310_DetailCellEditorWithYMonthBox(XF310 dialog) {
		super();
		dialog_ = dialog;
		dialog_.setActiveCellEditor(this);
		//
		GregorianCalendar calendar = new GregorianCalendar();
		int currentYear = calendar.get(Calendar.YEAR);
		int minimumYear = currentYear - 10;
		int maximumYear = currentYear + 10;
		//
		jComboBoxYear.setFont(new java.awt.Font("Dialog", 0, 11));
		jComboBoxYear.setBounds(new Rectangle(-1, 0, 50, 17));
		jComboBoxYear.addItem("");
		for (int i = minimumYear; i <= maximumYear; i++) {
			jComboBoxYear.addItem(String.valueOf(i));
		}
		jComboBoxYear.addItem("9999");
		jComboBoxYear.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
				setActiveCellEditor();
				if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jComboBoxYear.showPopup();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !jComboBoxYear.isPopupVisible()) {
					stopCellEditing();
				}
			}
		});
		//
		jComboBoxMonth.setFont(new java.awt.Font("Dialog", 0, 11));
		jComboBoxMonth.setBounds(new Rectangle(48, 0, 38, 17));
		jComboBoxMonth.addItem("");
		jComboBoxMonth.addItem("01");
		jComboBoxMonth.addItem("02");
		jComboBoxMonth.addItem("03");
		jComboBoxMonth.addItem("04");
		jComboBoxMonth.addItem("05");
		jComboBoxMonth.addItem("06");
		jComboBoxMonth.addItem("07");
		jComboBoxMonth.addItem("08");
		jComboBoxMonth.addItem("09");
		jComboBoxMonth.addItem("10");
		jComboBoxMonth.addItem("11");
		jComboBoxMonth.addItem("12");
		jComboBoxMonth.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jComboBoxMonth.showPopup();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !jComboBoxMonth.isPopupVisible()) {
					stopCellEditing();
					dialog_.getMessageList().clear();
					dialog_.checkErrorsToUpdate(true);
					dialog_.setMessagesOnPanel();
				}
				if (e.getKeyCode() == dialog_.getKeyCodeToUpdate() && !jComboBoxMonth.isPopupVisible()) {
					stopCellEditing();
					dialog_.getMessageList().clear();
					dialog_.checkErrorsToUpdate(false);
					dialog_.setMessagesOnPanel();
				}
			}
		});
		jComboBoxMonth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				stopCellEditing();
			}
		});
		//
		this.setSize(new Dimension(85, 17));
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(null);
		this.add(jComboBoxYear);
		this.add(jComboBoxMonth);
		//
	}
	
	void setActiveCellEditor() {
		dialog_.setActiveCellEditor(this);
	}

	public void addCellEditorListener(CellEditorListener listener) {
		listenerList.add(CellEditorListener.class, listener);
	} 

	public void removeCellEditorListener(CellEditorListener listener) {
		listenerList.remove(CellEditorListener.class, listener);
	} 

	protected void fireEditingStopped() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingStopped(changeEvent);
			}
		} 
	} 

	protected void fireEditingCanceled() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingCanceled(changeEvent);
			} 
		} 
	} 

	public void cancelCellEditing() {
	    fireEditingCanceled();
	} 

	public boolean stopCellEditing() {
		detailCell.setInternalValue(this.getInternalValue());
		fireEditingStopped();
		return true;
	} 

	public boolean isCellEditable(EventObject event) {
		return true;
	} 

	public boolean shouldSelectCell(EventObject event) {
		return true;
	} 

	public Object getCellEditorValue() {
		return detailCell;
	} 

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
		detailCell = (XF310_DetailCell)value;
		if (detailCell.isError()) {
			jComboBoxYear.setBackground(XFUtility.ERROR_COLOR);
			jComboBoxMonth.setBackground(XFUtility.ERROR_COLOR);
		} else {
			jComboBoxYear.setBackground(XFUtility.ACTIVE_COLOR);
			jComboBoxMonth.setBackground(XFUtility.ACTIVE_COLOR);
		}
		this.setValue(detailCell.getInternalValue());
		return this;
	}

	public Object getInternalValue() {
		String year = (String)jComboBoxYear.getItemAt(jComboBoxYear.getSelectedIndex());
		String month = (String)jComboBoxMonth.getItemAt(jComboBoxMonth.getSelectedIndex());
		return year + month;
	}

	public Object getExternalValue() {
		return this.getInternalValue();
	}
	
	public void setValue(Object obj) {
		String value = (String)obj;
		if (value != null) {
			value = value.trim();
		}
		if (value == null || value.equals("")) {
			jComboBoxYear.setSelectedIndex(0);
			jComboBoxMonth.setSelectedIndex(0);
		} else {
			if (value.length() == 6) {
				String yearValue = value.substring(0, 4);
				String monthValue = value.substring(4, 6);
				for (int i = 0; i < jComboBoxYear.getItemCount(); i++) {
					if (jComboBoxYear.getItemAt(i).equals(yearValue)) {
						jComboBoxYear.setSelectedIndex(i);
						break;
					}
				}
				for (int i = 0; i < jComboBoxMonth.getItemCount(); i++) {
					if (jComboBoxMonth.getItemAt(i).equals(monthValue)) {
						jComboBoxMonth.setSelectedIndex(i);
						break;
					}
				}
			}
		}
	}

	public void setBackground(Color color) {
		if (jComboBoxYear != null && jComboBoxMonth != null) {
			jComboBoxYear.setBackground(color);
			jComboBoxMonth.setBackground(color);
		}
	}
}

class XF310_DetailCellEditorWithFYearBox extends JPanel implements XFTableCellEditor {
	private static final long serialVersionUID = 1L;
	private JComboBox jComboBoxYear = new JComboBox();
	protected EventListenerList listenerList = new EventListenerList();
	protected ChangeEvent changeEvent = new ChangeEvent(this);
	private XF310_DetailCell detailCell = null;
	private XF310 dialog_ = null;

	public XF310_DetailCellEditorWithFYearBox(XF310 dialog) {
		super();
		dialog_ = dialog;
		//
		GregorianCalendar calendar = new GregorianCalendar();
		int currentYear = calendar.get(Calendar.YEAR);
		int minimumYear = currentYear - 10;
		int maximumYear = currentYear + 10;
		//
		jComboBoxYear.setFont(new java.awt.Font("Dialog", 0, 11));
		jComboBoxYear.setBounds(new Rectangle(-1, 0, 50, 17));
		jComboBoxYear.addItem("");
		for (int i = minimumYear; i <= maximumYear; i++) {
			jComboBoxYear.addItem(String.valueOf(i));
		}
		jComboBoxYear.addItem("9999");
		jComboBoxYear.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
				setActiveCellEditor();
				if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jComboBoxYear.showPopup();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !jComboBoxYear.isPopupVisible()) {
					stopCellEditing();
				}
			}
		});
		jComboBoxYear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				stopCellEditing();
			}
		});
		//
		this.setSize(new Dimension(50, 17));
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(null);
		this.add(jComboBoxYear);
		//
	}
	
	void setActiveCellEditor() {
		dialog_.setActiveCellEditor(this);
	}

	public void addCellEditorListener(CellEditorListener listener) {
		listenerList.add(CellEditorListener.class, listener);
	} 

	public void removeCellEditorListener(CellEditorListener listener) {
		listenerList.remove(CellEditorListener.class, listener);
	} 

	protected void fireEditingStopped() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingStopped(changeEvent);
			}
		} 
	} 

	protected void fireEditingCanceled() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingCanceled(changeEvent);
			} 
		} 
	} 

	public void cancelCellEditing() {
	    fireEditingCanceled();
	} 

	public boolean stopCellEditing() {
		detailCell.setInternalValue(this.getInternalValue());
		fireEditingStopped();
		return true;
	} 

	public boolean isCellEditable(EventObject event) {
		return true;
	} 

	public boolean shouldSelectCell(EventObject event) {
		return true;
	} 

	public Object getCellEditorValue() {
		return detailCell;
	} 

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
		detailCell = (XF310_DetailCell)value;
		if (detailCell.isError()) {
			jComboBoxYear.setBackground(XFUtility.ERROR_COLOR);
		} else {
			jComboBoxYear.setBackground(XFUtility.ACTIVE_COLOR);
		}
		this.setValue(detailCell.getInternalValue());
		return this;
	}

	public Object getInternalValue() {
		String year = (String)jComboBoxYear.getItemAt(jComboBoxYear.getSelectedIndex());
		return year;
	}

	public Object getExternalValue() {
		return this.getInternalValue();
	}
	
	public void setValue(Object obj) {
		String value = (String)obj;
		if (value != null) {
			value = value.trim();
		}
		if (value == null || value.equals("")) {
			jComboBoxYear.setSelectedIndex(0);
		} else {
			if (value.length() == 4) {
				for (int i = 0; i < jComboBoxYear.getItemCount(); i++) {
					if (jComboBoxYear.getItemAt(i).equals(value)) {
						jComboBoxYear.setSelectedIndex(i);
						break;
					}
				}
			}
		}
	}

	public void setBackground(Color color) {
		if (jComboBoxYear != null) {
			jComboBoxYear.setBackground(color);
		}
	}
}

class XF310_DetailCellEditorWithMSeqBox extends JPanel implements XFTableCellEditor {
	private static final long serialVersionUID = 1L;
	private JComboBox jComboBoxMSeq = new JComboBox();
	private ArrayList<Integer> listMSeq = new ArrayList<Integer>();
	protected EventListenerList listenerList = new EventListenerList();
	protected ChangeEvent changeEvent = new ChangeEvent(this);
	private XF310_DetailCell detailCell = null;
	private XF310 dialog_ = null;
    private String language = "";
    private String[] monthArrayEn = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov"};
    private String[] monthArrayJp = {"�P���x","�Q���x","�R���x","�S���x","�T���x","�U���x","�V���x","�W���x","�X���x","10���x","11���x","12���x","�P���x","�Q���x","�R���x","�S���x","�T���x","�U���x","�V���x","�W���x","�X���x","10���x","11���x"};
    private int startMonth = 1;

	public XF310_DetailCellEditorWithMSeqBox(XF310 dialog) {
		super();
		dialog_ = dialog;
		//
		language = dialog_.getSession().getDateFormat().substring(0, 2);
		//
		startMonth = dialog_.getSession().getSystemVariantInteger("FIRST_MONTH");
		//
		jComboBoxMSeq.setFont(new java.awt.Font("Dialog", 0, 12));
		jComboBoxMSeq.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
				setActiveCellEditor();
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jComboBoxMSeq.showPopup();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !jComboBoxMSeq.isPopupVisible()) {
					stopCellEditing();
					dialog_.getMessageList().clear();
					dialog_.checkErrorsToUpdate(true);
					dialog_.setMessagesOnPanel();
				}
				if (e.getKeyCode() == dialog_.getKeyCodeToUpdate() && !jComboBoxMSeq.isPopupVisible()) {
					stopCellEditing();
					dialog_.getMessageList().clear();
					dialog_.checkErrorsToUpdate(false);
					dialog_.setMessagesOnPanel();
				}
			}
		});
		jComboBoxMSeq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				stopCellEditing();
			}
		});
		//
		listMSeq.add(0);
		listMSeq.add(1);
		listMSeq.add(2);
		listMSeq.add(3);
		listMSeq.add(4);
		listMSeq.add(5);
		listMSeq.add(6);
		listMSeq.add(7);
		listMSeq.add(8);
		listMSeq.add(9);
		listMSeq.add(10);
		listMSeq.add(11);
		listMSeq.add(12);
		//
		if (language.equals("en")) {
			jComboBoxMSeq.setBounds(new Rectangle(0, 0, 50, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(50, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMSeq.addItem("");
			for (int i = startMonth -1; i < startMonth + 11; i++) {
				jComboBoxMSeq.addItem(monthArrayEn[i]);
			}
		}
		//
		if (language.equals("jp")) {
			jComboBoxMSeq.setBounds(new Rectangle(0, 0, 62, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(62, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMSeq.addItem("");
			for (int i = startMonth -1; i < startMonth + 11; i++) {
				jComboBoxMSeq.addItem(monthArrayJp[i]);
			}
		}
		//
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(null);
		this.add(jComboBoxMSeq);
	}
	
	void setActiveCellEditor() {
		dialog_.setActiveCellEditor(this);
	}

	public void addCellEditorListener(CellEditorListener listener) {
		listenerList.add(CellEditorListener.class, listener);
	} 

	public void removeCellEditorListener(CellEditorListener listener) {
		listenerList.remove(CellEditorListener.class, listener);
	} 

	protected void fireEditingStopped() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingStopped(changeEvent);
			}
		} 
	} 

	protected void fireEditingCanceled() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingCanceled(changeEvent);
			} 
		} 
	} 

	public void cancelCellEditing() {
	    fireEditingCanceled();
	} 

	public boolean stopCellEditing() {
		detailCell.setInternalValue(this.getInternalValue());
		fireEditingStopped();
		return true;
	} 

	public boolean isCellEditable(EventObject event) {
		return true;
	} 

	public boolean shouldSelectCell(EventObject event) {
		return true;
	} 

	public Object getCellEditorValue() {
		return detailCell;
	} 

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
		detailCell = (XF310_DetailCell)value;
		if (detailCell.isError()) {
			jComboBoxMSeq.setBackground(XFUtility.ERROR_COLOR);
		} else {
			jComboBoxMSeq.setBackground(XFUtility.ACTIVE_COLOR);
		}
		this.setValue(detailCell.getInternalValue());
		return this;
	}

	public Object getInternalValue() {
		return listMSeq.get(jComboBoxMSeq.getSelectedIndex());
	}

	public Object getExternalValue() {
		return (String)jComboBoxMSeq.getItemAt(jComboBoxMSeq.getSelectedIndex());
	}
	
	public void setValue(Object obj) {
		int value = 0;
		//
		try {
			value = Integer.parseInt(obj.toString());
		} catch (NumberFormatException e) {
		}
		//
		for (int i = 0; i < listMSeq.size(); i++) {
			if (listMSeq.get(i) == value) {
				jComboBoxMSeq.setSelectedIndex(i);
				break;
			}
		}
	}

	public void setBackground(Color color) {
		if (jComboBoxMSeq != null) {
			jComboBoxMSeq.setBackground(color);
		}
	}
}

class XF310_DetailCellEditorWithComboBox extends JComboBox implements XFTableCellEditor {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private String dataTypeOptions_ = "";
	private String tableAlias = "";
	private String fieldID = "";
	private String listType = "";
	private ArrayList<String> kubunKeyValueList = new ArrayList<String>();
	private ArrayList<XFHashMap> tableKeyValuesList = new ArrayList<XFHashMap>();
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private XF310_DetailReferTable referTable_ = null;
	private XF310 dialog_;
	protected EventListenerList listenerList = new EventListenerList();
	protected ChangeEvent changeEvent = new ChangeEvent(this);
	private XF310_DetailCell detailCell = null;

	public XF310_DetailCellEditorWithComboBox(String dataSourceName, String dataTypeOptions, XF310 dialog, XF310_DetailReferTable referTable, boolean isNullable) {
		super();
		//
		StringTokenizer workTokenizer;
		String wrk = "";
		String strWrk;
		//
		dataTypeOptions_ = dataTypeOptions;
		workTokenizer = new StringTokenizer(dataSourceName, "." );
		tableAlias = workTokenizer.nextToken();
		referTable_ = referTable;
		fieldID =workTokenizer.nextToken();
		dialog_ = dialog;
		//
		strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "VALUES");
		if (!strWrk.equals("")) {
			//
			listType = "VALUES_LIST";
			//
			if (isNullable) {
				this.addItem("");
			}
			//
			workTokenizer = new StringTokenizer(strWrk, ";" );
			while (workTokenizer.hasMoreTokens()) {
				wrk = workTokenizer.nextToken();
				this.addItem(wrk);
			}
		} else {
			strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "KUBUN");
			if (!strWrk.equals("")) {
				//
				listType = "KUBUN_LIST";
				//
				if (isNullable) {
					kubunKeyValueList.add("");
					this.addItem("");
				}
				//
				try {
					Statement statement = dialog_.getSession().getConnection().createStatement();
					ResultSet result = statement.executeQuery("select KBUSERKUBUN,TXUSERKUBUN  from " + dialog_.getSession().getTableNameOfUserVariants() + " where IDUSERKUBUN = '" + strWrk + "' order by SQLIST");
					while (result.next()) {
						//
						kubunKeyValueList.add(result.getString("KBUSERKUBUN").trim());
						wrk = result.getString("TXUSERKUBUN").trim();
						this.addItem(wrk);
					}
					result.close();
					//
					if (this.getItemCount() == 0) {
						JOptionPane.showMessageDialog(null, res.getString("FunctionError24") + dataSourceName + res.getString("FunctionError25"));
					}
				} catch (SQLException e) {
					e.printStackTrace(dialog_.getExceptionStream());
					dialog_.setErrorAndCloseFunction();
				}
			} else {
				//
				if (referTable_ != null) {
					listType = "RECORDS_LIST";
					keyFieldList = referTable_.getKeyFieldIDList();
				}
			}
		}
		//
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.setFocusable(false);
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				stopCellEditing();
			}
		});
		this.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
				setActiveCellEditor();
				if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						showPopup();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !isPopupVisible()) {
					stopCellEditing();
					dialog_.getMessageList().clear();
					dialog_.checkErrorsToUpdate(true);
					dialog_.setMessagesOnPanel();
				}
				if (e.getKeyCode() == dialog_.getKeyCodeToUpdate() && !isPopupVisible()) {
					stopCellEditing();
					dialog_.getMessageList().clear();
					dialog_.checkErrorsToUpdate(false);
					dialog_.setMessagesOnPanel();
				}
			}
		});
	}
	
	void setActiveCellEditor() {
		dialog_.setActiveCellEditor(this);
	}

	public void addCellEditorListener(CellEditorListener listener) {
		listenerList.add(CellEditorListener.class, listener);
	} 

	public void removeCellEditorListener(CellEditorListener listener) {
		listenerList.remove(CellEditorListener.class, listener);
	} 

	protected void fireEditingStopped() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingStopped(changeEvent);
			}
		} 
	} 

	protected void fireEditingCanceled() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingCanceled(changeEvent);
			} 
		} 
	} 

	public void cancelCellEditing() {
	    fireEditingCanceled();
	} 

	public boolean stopCellEditing() {
		detailCell.setInternalValue(this.getInternalValue());
		fireEditingStopped();
		return true;
	} 

	public boolean isCellEditable(EventObject event) {
		return true;
	} 

	public boolean shouldSelectCell(EventObject event) {
		return true;
	} 

	public Object getCellEditorValue() {
		return detailCell;
	} 

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
		detailCell = (XF310_DetailCell)value;
		if (detailCell.isError()) {
			this.setBackground(XFUtility.ERROR_COLOR);
		} else {
			this.setBackground(XFUtility.ACTIVE_COLOR);
		}
		//
		this.setupRecordList();
		this.setValue((String)detailCell.getInternalValue());
		//
		return this;
	}

	public void setupRecordList() {
		if (referTable_ != null && listType.equals("RECORDS_LIST")) {
			//
			detailCell.getTableRowNumber().setValueToDetailColumns();
			//
			tableKeyValuesList.clear();
			this.removeAllItems();
			//
			boolean blankItemRequired = true;
			XFHashMap keyValues = new XFHashMap();
			for (int i = 0; i < referTable_.getWithKeyFieldIDList().size(); i++) {
				for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
					if (referTable_.getWithKeyFieldIDList().get(i).equals(dialog_.getDetailColumnList().get(j).getTableAlias() + "." + dialog_.getDetailColumnList().get(j).getFieldID())) {
						if (dialog_.getDetailColumnList().get(j).isNullable()) {
							//keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getDetailColumnList().get(j).getNullValue());
							if (dialog_.getDetailColumnList().get(j).isVisibleOnPanel()) {
								keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getDetailColumnList().get(j).getValue());
							} else {
								keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getDetailColumnList().get(j).getNullValue());
							}
						} else {
							blankItemRequired = false;
							break;
						}
					}
				}
				if (!blankItemRequired) {
					break;
				}
			}
			if (blankItemRequired) {
				tableKeyValuesList.add(keyValues);
				this.addItem("");
			}
			//
			try {
				String wrk = "";
				String sql = referTable_.getSelectSQL(true);
				//XFUtility.appendLog(sql, dialog_.getProcessLog());
				dialog_.setProcessLog(sql);
				Statement statement = dialog_.getSession().getConnection().createStatement();
				ResultSet result = statement.executeQuery(sql);
				while (result.next()) {
					keyValues = new XFHashMap();
					//
					for (int i = 0; i < keyFieldList.size(); i++) {
						keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), result.getString(keyFieldList.get(i)));
					}
					//
					wrk = result.getString(fieldID).trim();
					keyValues.addValue(tableAlias + "." + fieldID, wrk);
					tableKeyValuesList.add(keyValues);
					this.addItem(wrk);
				}
				result.close();
				//
			} catch (SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
	}

	public Object getInternalValue() {
		Object value = "";
		//
		if (listType.equals("VALUES_LIST")) {
			value = this.getItemAt(this.getSelectedIndex()).toString();
		}
		if (listType.equals("KUBUN_LIST")) {
			value = kubunKeyValueList.get(this.getSelectedIndex());
		}
		if (listType.equals("RECORDS_LIST")) {
			if (tableKeyValuesList.size() > 0) {
				value = tableKeyValuesList.get(this.getSelectedIndex());
			}
		}
		//
		return value;
	}

	public Object getExternalValue() {
		return this.getItemAt(this.getSelectedIndex()).toString();
	}
	
	public void setValue(Object obj) {
		String value = (String)obj;
		value = value.trim();
		if (!this.isEditable) {
			if (listType.equals("VALUES_LIST")) {
				for (int i = 0; i < this.getItemCount(); i++) {
					if (this.getItemAt(i).toString().equals(value)) {
						this.setSelectedIndex(i);
						break;
					}
				}
			}
			if (listType.equals("KUBUN_LIST")) {
				for (int i = 0; i < kubunKeyValueList.size(); i++) {
					if (kubunKeyValueList.get(i).equals(value)) {
						this.setSelectedIndex(i);
						break;
					}
				}
			}
			if (listType.equals("RECORDS_LIST")) {
				if (this.getItemCount() > 0) {
					if (value == null || value.equals("")) {
					} else {
						for (int i = 0; i < this.getItemCount(); i++) {
							if (this.getItemAt(i).toString().equals(value)) {
								this.setSelectedIndex(i);
								break;
							}
						}
					}
				}
			}
		}
	}
}

class XF310_DetailCellEditorWithPromptCall extends JPanel implements XFTableCellEditor {
	private static final long serialVersionUID = 1L;
	private String tableAlias = "";
	private String fieldID = "";
	private JTextField jTextField;
	private JButton jButton = new JButton();
    private XF310 dialog_;
    private org.w3c.dom.Element functionElement_;
    private org.w3c.dom.Element fieldElement_;
	protected EventListenerList listenerList = new EventListenerList();
	protected ChangeEvent changeEvent = new ChangeEvent(this);
	private XF310_DetailCell detailCell = null;
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();

	public XF310_DetailCellEditorWithPromptCall(org.w3c.dom.Element fieldElement, org.w3c.dom.Element functionElement, XF310 dialog){
		super();
		//
		fieldElement_ = fieldElement;
		functionElement_ = functionElement;
		dialog_ = dialog;
		//
		StringTokenizer workTokenizer = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		fieldID =workTokenizer.nextToken();
		//
		jTextField = new JTextField();
		jTextField.setEditable(false);
		jTextField.setFont(new java.awt.Font("Monospaced", 0, 11));
		//
		String fieldOptions = fieldElement_.getAttribute("FieldOptions");
		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_PUT");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToPutList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_PUT_TO");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToPutToList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_GET");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToGetList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_GET_TO");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToGetToList_.add(workTokenizer.nextToken());
			}
		}
		//
		jButton.setText("...");
		jButton.setFont(new java.awt.Font("Dialog", 0, 11));
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object value;
				//
				HashMap<String, Object> parmValueMap = new HashMap<String, Object>();
				HashMap<String, Object> columnValueMap = detailCell.getTableRowNumber().getColumnValueMap();
				for (int i = 0; i < fieldsToPutList_.size(); i++) {
					value = columnValueMap.get(fieldsToPutList_.get(i));
					if (value != null) {
						parmValueMap.put(fieldsToPutToList_.get(i), value);
					}
				}
				if (parmValueMap.size() < fieldsToPutList_.size()) {
					for (int i = 0; i < fieldsToPutList_.size(); i++) {
						value = dialog_.getValueOfHeaderFieldByName(fieldsToPutList_.get(i));
						if (value != null) {
							parmValueMap.put(fieldsToPutToList_.get(i), value);
						}
					}
				}
				//
				HashMap<String, Object> returnMap = dialog_.getSession().getFunction().execute(functionElement_, parmValueMap);
				if (!returnMap.get("RETURN_CODE").equals("99")) {
					HashMap<String, Object> fieldsToGetMap = new HashMap<String, Object>();
					for (int i = 0; i < fieldsToGetList_.size(); i++) {
						value = returnMap.get(fieldsToGetList_.get(i));
						if (value != null) {
							fieldsToGetMap.put(fieldsToGetToList_.get(i), value);
						}
					}
					String dataSourceName;
					for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
						dataSourceName = dialog_.getDetailColumnList().get(i).getDataSourceName();
						if (fieldsToGetMap.containsKey(dataSourceName) && columnValueMap.containsKey(dataSourceName)) {
							columnValueMap.put(dataSourceName, fieldsToGetMap.get(dataSourceName));
							if (dataSourceName.equals(tableAlias + "." + fieldID)) {
								setValue(fieldsToGetMap.get(dataSourceName));
							}
						}
					}
				}
				stopCellEditing();
				dialog_.getTableModel().fireTableRowsUpdated(dialog_.getJTableMain().getSelectedRow(), dialog_.getJTableMain().getSelectedRow());
			}
		});
		jButton.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jButton.doClick();
					}
				}
			}
		});
		//
		this.setLayout(new BorderLayout());
		this.add(jTextField, BorderLayout.CENTER);
		this.add(jButton, BorderLayout.EAST);
	}
	
	public boolean hasEditControlledKey() {
		boolean anyOfKeysAreEditControlled = false;
		//
		for (int i = 0; i < fieldsToGetToList_.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (fieldsToGetToList_.get(i).equals(dialog_.getDetailColumnList().get(j).getTableAlias() + "." + dialog_.getDetailColumnList().get(j).getFieldID())) {
					if (!dialog_.getDetailColumnList().get(j).isEditable() && dialog_.getDetailTable().getTableID().equals(dialog_.getDetailColumnList().get(j).getTableAlias())) {
						anyOfKeysAreEditControlled = true;
						break;
					}
				}
			}
		}
		//
		return anyOfKeysAreEditControlled;
	}

	public void addCellEditorListener(CellEditorListener listener) {
		listenerList.add(CellEditorListener.class, listener);
	} 

	public void removeCellEditorListener(CellEditorListener listener) {
		listenerList.remove(CellEditorListener.class, listener);
	} 

	protected void fireEditingStopped() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingStopped(changeEvent);
			}
		} 
	} 

	protected void fireEditingCanceled() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingCanceled(changeEvent);
			} 
		} 
	} 

	public void cancelCellEditing() {
	    fireEditingCanceled();
	} 

	public boolean stopCellEditing() {
		detailCell.setInternalValue(this.getInternalValue());
		fireEditingStopped();
		return true;
	} 

	public boolean isCellEditable(EventObject event) {
		return true;
	} 

	public boolean shouldSelectCell(EventObject event) {
		return true;
	} 

	public Object getCellEditorValue() {
		return detailCell;
	} 

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
		detailCell = (XF310_DetailCell)value;
		if (detailCell.isError()) {
			this.setBackground(XFUtility.ERROR_COLOR);
		} else {
			this.setBackground(XFUtility.ACTIVE_COLOR);
		}
		//this.setValue((String)detailCell.getInternalValue());
		this.setValue(detailCell.getInternalValue().toString());
		return this;
	}

	public Object getInternalValue() {
		return jTextField.getText();
	}

	public Object getExternalValue() {
		return jTextField.getText();
	}
	
	public void setValue(Object obj) {
		if (obj == null) {
			jTextField.setText("");
		} else {
			jTextField.setText(obj.toString());
		}
	}
}
	
@SuppressWarnings("unchecked")
class XF310_DetailRowNumber extends Object {
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private int number_;
	private String recordType_ = "";
	private HashMap<String, Object> keyValueMap_, columnValueMap_, columnOldValueMap_;
	private DecimalFormat integerFormat = new DecimalFormat("0000");
	private XF310 dialog_ = null;

	public XF310_DetailRowNumber(int num, String recordType, HashMap<String, Object> keyValueMap, HashMap<String, Object> columnValueMap, XF310 dialog) {
		number_ = num;
		keyValueMap_ = keyValueMap;
		recordType_ = recordType;
		columnValueMap_ = columnValueMap;
		columnOldValueMap_ = (HashMap<String, Object>)columnValueMap_.clone();
		dialog_ = dialog;
	}

	public String getRecordType() {
		return recordType_;
	}

	public String toString() {
		return integerFormat.format(number_);
	}

	public HashMap<String, Object> getKeyValueMap() {
		return keyValueMap_;
	}

	public HashMap<String, Object> getColumnValueMap() {
		return columnValueMap_;
	}

	public HashMap<String, Object> getColumnOldValueMap() {
		return columnOldValueMap_;
	}

	public void setRecordType(String type) {
		recordType_ = type;
	}

	public void setValueToDetailColumns() {
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			dialog_.getDetailColumnList().get(i).setValue(columnValueMap_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
			dialog_.getDetailColumnList().get(i).setOldValue(columnOldValueMap_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
		}
	}

	public int countErrors(ArrayList<String> messageList) {
		int countOfErrors = 0;
		XF310_DetailCell cell = null;
		int columnIndex;
		//
		String event = "";
		if (recordType_.equals("NEW")) {
			event = "BC";
		}
		if (recordType_.equals("CURRENT")) {
			event = "BU";
		}
		//
		// Reset Error Status //
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			dialog_.getDetailColumnList().get(i).setEditable(true);
			dialog_.getDetailColumnList().get(i).setError(false);
			if (dialog_.getDetailColumnList().get(i).isVisibleOnPanel()) {
				columnIndex = dialog_.getDetailColumnList().get(i).getColumnIndex();
				cell = (XF310_DetailCell)dialog_.getTableModel().getValueAt(this.getRowIndex(), columnIndex);
				cell.setError(false);
			}
		}
		//
		countOfErrors = dialog_.fetchDetailReferRecords(event, true, "", columnValueMap_, columnOldValueMap_);
		//
		// Check Null-Constraints //
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			columnValueMap_.put(dialog_.getDetailColumnList().get(i).getDataSourceName(), dialog_.getDetailColumnList().get(i).getInternalValue());
			if (dialog_.getDetailColumnList().get(i).getTableAlias().equals(dialog_.getDetailTable().getTableID())
					&& dialog_.getDetailColumnList().get(i).isVisibleOnPanel()
					&& dialog_.getDetailColumnList().get(i).isEditable()) {
				if (dialog_.getDetailColumnList().get(i).isNullError(columnValueMap_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()))) {
					countOfErrors++;
				}
			}
		}
		//
		// Set cells EditControlled //
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isVisibleOnPanel()) {
				columnIndex = dialog_.getDetailColumnList().get(i).getColumnIndex();
				if (dialog_.getDetailColumnList().get(i).isEditable()) {
					dialog_.getTableModel().setEditable(this.getRowIndex(), columnIndex);
				} else {
					cell = (XF310_DetailCell)dialog_.getTableModel().getValueAt(this.getRowIndex(), columnIndex);
					cell.setEditable(false);
				}
			}
		}
		//
		// Set Errors on Cells //
		int rowNumber;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isError()) {
				if (dialog_.getDetailColumnList().get(i).isVisibleOnPanel() && dialog_.getDetailColumnList().get(i).isEditable()) {
					cell = (XF310_DetailCell)dialog_.getTableModel().getValueAt(this.getRowIndex(), dialog_.getDetailColumnList().get(i).getColumnIndex());
					cell.setError(true);
				}
				rowNumber = this.getRowIndex() + 1;
				messageList.add(dialog_.getDetailColumnList().get(i).getCaption() + res.getString("LineNumber1") + rowNumber + res.getString("LineNumber2") + dialog_.getDetailColumnList().get(i).getError());
			}
		}
		//
		return countOfErrors;
	}

	public int getRowIndex() {
		int index = -1;
		XF310_DetailRowNumber rowNumber = null;
		for (int i = 0; i < dialog_.getTableModel().getRowCount(); i++) {
			rowNumber = (XF310_DetailRowNumber)dialog_.getTableModel().getValueAt(i, 0);
			if (rowNumber.equals(this)) {
				index = i;
			}
		}
		return index;
	}

	public void setNumber(int num) {
		number_ = num;
	}
}

class XF310_DetailCell extends Object {
	private static final long serialVersionUID = 1L;
	private XF310_DetailColumn column_ = null;
	private XF310_DetailRowNumber rowNumber_ = null;
	private Color foreground = Color.black;
	private boolean isError_ = false;
	private boolean isEditable_ = true;
	//
	public XF310_DetailCell(XF310_DetailColumn column, XF310_DetailRowNumber rowNumber) {
		column_ = column;
		isEditable_ = column.isEditable();
		rowNumber_ = rowNumber;
	}
	public Object getInternalValue() {
		column_.setValue(rowNumber_.getColumnValueMap().get(column_.getDataSourceName()));
		return column_.getInternalValue();
	}
	public Object getExternalValue() {
		column_.setValue(rowNumber_.getColumnValueMap().get(column_.getDataSourceName()));
		return column_.getExternalValue();
	}
	public void setInternalValue(Object value) {
		if (value instanceof XFHashMap) {
			XFHashMap keyValues = (XFHashMap)value;
			for (int i = 0; i < keyValues.size(); i++) {
				rowNumber_.getColumnValueMap().put(keyValues.getKeyIDByIndex(i), keyValues.getValueByIndex(i));
			}
		} else {
			rowNumber_.getColumnValueMap().put(column_.getDataSourceName(), value);
		}
	}
	public XF310_DetailRowNumber getTableRowNumber() {
		return rowNumber_;
	}
	public void setForeground(Color color) {
		foreground = color;
	}
	public Color getForeground() {
		return foreground;
	}
	public XF310_DetailColumn getDetailColumn() {
		return column_;
	}
	public void setError(boolean isError) {
		isError_ = isError;
	}
	public boolean isError() {
		return isError_;
	}
	public void setEditable(boolean editable) {
		isEditable_ = editable;
	}
	public boolean isEditable() {
		return isEditable_;
	}
}

class XF310_DetailColumn extends Object implements XFScriptableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element functionColumnElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF310 dialog_ = null;
	private String tableID_ = "";
	private String tableAlias_ = "";
	private String fieldID_ = "";
	private String dataType = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private ArrayList<String> fieldOptionList;
	private String fieldOptions = "";
	private String fieldCaption = "";
	private int fieldWidth = 50;
	private int columnIndex = -1;
	private boolean isKey = false;
	private boolean isNullable = true;
	private boolean isFieldOnDetailTable = false;
	private boolean isVisibleOnPanel = true;
	private boolean isVirtualField = false;
	private boolean isEditable = true;
	private boolean isNonEditableField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private DecimalFormat integerFormat = new DecimalFormat("#,##0");
	private DecimalFormat floatFormat0 = new DecimalFormat("#,##0");
	private DecimalFormat floatFormat1 = new DecimalFormat("#,##0.0");
	private DecimalFormat floatFormat2 = new DecimalFormat("#,##0.00");
	private DecimalFormat floatFormat3 = new DecimalFormat("#,##0.000");
	private DecimalFormat floatFormat4 = new DecimalFormat("#,##0.0000");
	private DecimalFormat floatFormat5 = new DecimalFormat("#,##0.00000");
	private DecimalFormat floatFormat6 = new DecimalFormat("#,##0.000000");
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();
	private org.w3c.dom.Element promptFunctionElement_ = null;
	private Object value_ = null;
	private Object oldValue_ = null;
	private Color foreground = Color.black;
	private XFTableCellEditor editor = null;
	private XF310_DetailCellRenderer renderer = null;
	private boolean isError = false;
	private String errorMessage = "";
	private String numberingID = "";

	public XF310_DetailColumn(org.w3c.dom.Element functionColumnElement, XF310 dialog){
		super();
		//
		String wrkStr;
		functionColumnElement_ = functionColumnElement;
		dialog_ = dialog;
		fieldOptions = functionColumnElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);
		//
		StringTokenizer workTokenizer = new StringTokenizer(functionColumnElement_.getAttribute("DataSource"), "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog.getTableIDOfTableAlias(tableAlias_);
		fieldID_ =workTokenizer.nextToken();
		//
		if (tableID_.equals(dialog_.getDetailTable().getTableID()) && tableID_.equals(tableAlias_)) {
			isFieldOnDetailTable = true;
			ArrayList<String> keyNameList = dialog_.getDetailTable().getKeyFieldIDList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					isNonEditableField = true;
					break;
				}
			}
		} else {
			wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
			if (!fieldOptionList.contains("PROMPT_LIST") && wrkStr.equals("")) {
				isNonEditableField = true;
			}
		}
		//
		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + res.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = wrkStr;
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		//if (dataSize > 50) {
		//	dataSize = 50;
		//}
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			if (!fieldOptionList.contains("PROMPT_LIST")) {
				isNullable = false;
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			numberingID = wrkStr;
			isNonEditableField = true;
		}
		//
		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}
		//
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		JLabel jLabel = new JLabel();
		FontMetrics metrics = jLabel.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		int captionWidth = metrics.stringWidth(fieldCaption) + 18;
		//
		String basicType = this.getBasicType();
		//
		if (fieldOptionList.contains("PROMPT_LIST")) {
			XF310_DetailReferTable referTable = null;
			ArrayList<XF310_DetailReferTable> referTableList = dialog_.getDetailReferTableList();
			for (int i = 0; i < referTableList.size(); i++) {
				if (referTableList.get(i).getTableID().equals(tableID_)) {
					if (referTableList.get(i).getTableAlias().equals("") || referTableList.get(i).getTableAlias().equals(tableAlias_)) {
						referTable = referTableList.get(i);
						break;
					}
				}
			}
			if (dataTypeOptionList.contains("KANJI")) {
				fieldWidth = dataSize * 14 + 5;
			} else {
				fieldWidth = dataSize * 7 + 12;
			}
			if (!isNonEditableField) {
				editor = new XF310_DetailCellEditorWithComboBox(functionColumnElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, referTable, isNullable);
			}
		} else {
			wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
			if (!wrkStr.equals("")) {
				NodeList functionList = dialog_.getSession().getFunctionList();
				for (int k = 0; k < functionList.getLength(); k++) {
					workElement = (org.w3c.dom.Element)functionList.item(k);
					if (workElement.getAttribute("ID").equals(wrkStr)) {
						promptFunctionElement_ = workElement;
						break;
					}
				}
				if (dataTypeOptionList.contains("KANJI")) {
					fieldWidth = dataSize * 14 + 5;
				} else {
					fieldWidth = dataSize * 7 + 12;
				}
				if (!isNonEditableField) {
					editor = new XF310_DetailCellEditorWithPromptCall(functionColumnElement_, promptFunctionElement_, dialog_);
				}
			} else {
				wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
				if (!wrkStr.equals("")) {
					try {
						String wrk = "";
						Statement statement = dialog_.getSession().getConnection().createStatement();
						ResultSet result = statement.executeQuery("select * from " + dialog_.getSession().getTableNameOfUserVariants() + " where IDUSERKUBUN = '" + wrkStr + "'");
						while (result.next()) {
							//
							kubunValueList.add(result.getString("KBUSERKUBUN").trim());
							wrk = result.getString("TXUSERKUBUN").trim();
							if (metrics.stringWidth(wrk) + 10 > fieldWidth) {
								fieldWidth = metrics.stringWidth(wrk) + 10;
							}
							kubunTextList.add(wrk);
						}
						result.close();
						if (!isNonEditableField) {
							editor = new XF310_DetailCellEditorWithComboBox(functionColumnElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, null, isNullable);
						}
					} catch (SQLException e) {
						e.printStackTrace(dialog_.getExceptionStream());
						dialog_.setErrorAndCloseFunction();
					}
				} else {
					if (dataTypeOptionList.contains("KANJI")) {
						fieldWidth = dataSize * 14 + 5;
						if (!isNonEditableField) {
							editor = new XF310_DetailCellEditorWithTextField(this, dialog_);
						}
					} else {
						if (dataTypeOptionList.contains("YMONTH")) {
							fieldWidth = 85;
							if (!isNonEditableField) {
								editor = new XF310_DetailCellEditorWithYMonthBox(dialog_);
							}
						} else {
							if (dataTypeOptionList.contains("MSEQ")) {
								fieldWidth = 70;
								if (!isNonEditableField) {
									editor = new XF310_DetailCellEditorWithMSeqBox(dialog_);
								}
							} else {
								if (dataTypeOptionList.contains("FYEAR")) {
									fieldWidth = 80;
									if (!isNonEditableField) {
										editor = new XF310_DetailCellEditorWithFYearBox(dialog_);
									}
								} else {
									if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
										fieldWidth = XFUtility.getLengthOfEdittedNumericValue(dataSize, decimalSize, dataTypeOptionList.contains("ACCEPT_MINUS")) * 7 + 21;
										if (!isNonEditableField) {
											editor = new XF310_DetailCellEditorWithTextField(this, dialog_);
										}
									} else {
										if (basicType.equals("DATE")) {
											fieldWidth = XFUtility.getWidthOfDateValue(dialog_.getSession().getDateFormat());
											if (!isNonEditableField) {
												editor = new XF310_DetailCellEditorWithDateField(dialog_);
											}
										} else {
											fieldWidth = dataSize * 7 + 12;
											if (!isNonEditableField) {
												editor = new XF310_DetailCellEditorWithTextField(this, dialog_);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		//
		isEditable = !isNonEditableField;
		//
		if (fieldWidth > 320) {
			fieldWidth = 320;
		}
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		}
		//
		if (captionWidth > fieldWidth) {
			fieldWidth = captionWidth;
		}
		//
		renderer = new XF310_DetailCellRenderer();
		if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
			renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		} else {
			renderer.setHorizontalAlignment(SwingConstants.LEFT);
		}
		//
		if (!dialog_.getEngineScriptBindings().containsKey(this.getFieldIDInScript())) {
			dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
		}
	}

	public XF310_DetailColumn(String tableID, String tableAlias, String fieldID, XF310 dialog){
		super();
		//
		functionColumnElement_ = null;
		dialog_ = dialog;
		fieldOptions = "";
		fieldOptionList = new ArrayList<String>();
		//
		isVisibleOnPanel = false;
		//
		tableID_ = tableID;
		if (tableAlias.equals("")) {
			tableAlias_ = tableID;
		} else {
			tableAlias_ = tableAlias;
		}
		fieldID_ = fieldID;
		//
		if (tableID_.equals(dialog_.getDetailTable().getTableID()) && tableID_.equals(tableAlias_)) {
			isFieldOnDetailTable = true;
			ArrayList<String> keyNameList = dialog_.getDetailTable().getKeyFieldIDList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					isNonEditableField = true;
					break;
				}
			}
		} else {
			isNonEditableField = true;
		}
		//
		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + res.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		//if (dataSize > 50) {
		//	dataSize = 50;
		//}
		if (workElement.getAttribute("Nullable").equals("F")) {
				isNullable = false;
		}
		//
		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			StringTokenizer workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}
		//
		String wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			numberingID = wrkStr;
			isNonEditableField = true;
		}
		//
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		isEditable = !isNonEditableField;
		//
		if (!dialog_.getEngineScriptBindings().containsKey(this.getFieldIDInScript())) {
			dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
		}
	}

	public void checkPromptKeyEdit() {
		if (!XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL").equals("")
				&& !isNonEditableField) {
			if (((XF310_DetailCellEditorWithPromptCall)editor).hasEditControlledKey()) {
				this.setEditable(false);
			}
		}
	}
	
	public boolean isNullError(Object object){
		String basicType = this.getBasicType();
		String strWrk;
		boolean isNullError = false;
		//
		if (basicType.equals("INTEGER")) {
			if (object.equals("")) {
				object = "0";
			}
			int value = Integer.parseInt(object.toString());
			if (!this.isNullable) {
				if (value == 0) {
					isNullError = true;
				}
			}
		}
		if (basicType.equals("FLOAT")) {
			if (object.equals("")) {
				object = "0.0";
			}
			double value = Double.parseDouble(object.toString());
			if (!this.isNullable) {
				if (value == 0) {
					isNullError = true;
				}
			}
		}
		if (basicType.equals("DATE")) {
			if ((object == null || object.equals("")) && !this.isNullable) {
				isNullError = true;
			}
		}
		if (basicType.equals("STRING")) {
			strWrk = "";
			if (object != null) {
				strWrk = object.toString();
			}
			if (strWrk.equals("") && !this.isNullable) {
				isNullError = true;
			}
			if (dataTypeOptionList.contains("YMONTH") && strWrk.length() > 0 && strWrk.length() < 6) {
				isNullError = true;
			}
			if (dataTypeOptionList.contains("FYEAR") && strWrk.length() > 0 && strWrk.length() < 4) {
				isNullError = true;
			}
		}
		//
		if (isNullError) {
			this.setError(res.getString("FunctionError16"));
		}
		//
		return isNullError;
	}

	public void setError(boolean error) {
		if (error) {
			isError = true;
		} else {
			isError = false;
			this.errorMessage = "";
		}
	}

	public String getError(){
		return errorMessage;
	}

	public void setError(String message){
		if (!message.equals("") && this.errorMessage.equals("")) {
			setError(true);
			this.errorMessage = message;
		}
	}

	public boolean isError() {
		return isError;
	}

	public boolean isVisibleOnPanel(){
		return isVisibleOnPanel;
	}

	public boolean isVirtualField(){
		return isVirtualField;
	}

	public boolean isRangeKeyFieldValid(){
		return isRangeKeyFieldValid;
	}

	public boolean isRangeKeyFieldExpire(){
		return isRangeKeyFieldExpire;
	}

	public org.w3c.dom.Element getTableElement(){
		return tableElement;
	}

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
	}

	public String getTableID(){
		return tableID_;
	}

	public String getFieldID(){
		return fieldID_;
	}

	public String getFieldIDInScript(){
		return tableAlias_ + "_" + fieldID_;
	}

	public String getTableAlias(){
		return tableAlias_;
	}

	public String getDataSourceName(){
		return tableAlias_ + "." + fieldID_;
	}

	public boolean isKey(){
		return isKey;
	}

	public boolean isFieldOnDetailTable(){
		return isFieldOnDetailTable;
	}

	public Object getInternalValue(){
		return value_;
	}

	public Object getExternalValue(){
		Object value = null;
		String basicType = this.getBasicType();
		//
		if (basicType.equals("INTEGER")) {
			if (value_ == null || value_.toString().equals("")) {
				value = "";
			} else {
				String wrkStr = value_.toString();
				int pos = wrkStr.indexOf(".");
				if (pos >= 0) {
					wrkStr = wrkStr.substring(0, pos);
				}
				value = integerFormat.format(Integer.parseInt(wrkStr));
			}
		} else {
			if (basicType.equals("FLOAT")) {
				double doubleWrk = 0;
				if (value_ != null && !value_.toString().equals("")) {
					doubleWrk = Double.parseDouble(value_.toString());
				}
				if (decimalSize == 0) {
					value = floatFormat0.format(doubleWrk);
				}
				if (decimalSize == 1) {
					value = floatFormat1.format(doubleWrk);
				}
				if (decimalSize == 2) {
					value = floatFormat2.format(doubleWrk);
				}
				if (decimalSize == 3) {
					value = floatFormat3.format(doubleWrk);
				}
				if (decimalSize == 4) {
					value = floatFormat4.format(doubleWrk);
				}
				if (decimalSize == 5) {
					value = floatFormat5.format(doubleWrk);
				}
				if (decimalSize == 6) {
					value = floatFormat6.format(doubleWrk);
				}

			} else {
				if (basicType.equals("DATE")) {
					if (value_ == null || value_.equals("")) {
						value = "";
					} else {
						value = XFUtility.getUserExpressionOfUtilDate(XFUtility.convertDateFromSqlToUtil(java.sql.Date.valueOf(value_.toString())), dialog_.getSession().getDateFormat(), false);
					}
				} else {
					if (basicType.equals("DATETIME")) {
						if (value_ != null) {
							value = value_.toString().replace("-", "/");
						}
					} else {
						if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN").equals("")) {
							if (value_ == null || value_.toString().trim().equals("")) {
								value = "";
							} else {
								String wrkStr = value_.toString().trim();
								for (int i = 0; i < kubunValueList.size(); i++) {
									if (kubunValueList.get(i).equals(wrkStr)) {
										value = kubunTextList.get(i);
										break;
									}
								}
							}
						} else {
							if (value_ == null) {
								value = "";
							} else {
								value = value_.toString().trim();
								//
								if (dataTypeOptionList.contains("YMONTH") || dataTypeOptionList.contains("FYEAR")) {
									String wrkStr = (String)value.toString();
									if (!wrkStr.equals("")) {
										value = XFUtility.getUserExpressionOfYearMonth(wrkStr, dialog_.getSession().getDateFormat());
									}
								}
								//
								if (dataTypeOptionList.contains("MSEQ")) {
									value = XFUtility.getUserExpressionOfMSeq(Integer.parseInt(value.toString()), dialog_.getSession());
								}
							}
						}
					}
				}
			}
		}
		//
		return value;
	}

	public boolean isNull(){
		return XFUtility.isNullValue(this.getBasicType(), value_);
	}

	public boolean isNullable() {
		return isNullable;
	}

	public Object getNullValue(){
		return XFUtility.getNullValueOfBasicType(this.getBasicType());
	}

	public boolean isAutoNumberField(){
		return !numberingID.equals("");
	}

	public Object getAutoNumberValue(){
		Object value = null;
		//
		if (!numberingID.equals("")) {
			value = dialog_.getSession().getNextNumber(numberingID);
		}
		//
		return value;
	}

	public String getDataTypeOptions() {
		return dataTypeOptions;
	}

	public String getFieldOptions() {
		return fieldOptions;
	}

	public XF310_DetailCellRenderer getCellRenderer(){
		return renderer;
	}

	public TableCellEditor getCellEditor(){
		return editor;
	}

	public String getCaption(){
		return fieldCaption;
	}

	public int getDataSize(){
		return dataSize;
	}

	public int getDecimalSize(){
		return decimalSize;
	}

	public int getWidth(){
		return fieldWidth;
	}

	public int getColumnIndex(){
		return columnIndex;
	}

	public void setColumnIndex(int index){
		columnIndex = index;
	}

	public void setValueOfResultSet(ResultSet result) {
		String basicType = this.getBasicType();
		this.setColor("");
		//
		try {
			if (this.isVirtualField) {
				if (this.isRangeKeyFieldExpire()) {
					value_ = XFUtility.calculateExpireValue(this.getTableElement(), result, dialog_.getSession());
				}
			} else {
				//
				Object value = result.getObject(this.getFieldID()); 
				//
//				if (this.isFieldOnDetailTable) {
//					value_ = value;
//				} else {
					if (basicType.equals("INTEGER")) {
						if (value == null || value.equals("")) {
							value_ = "";
						} else {
							String wrkStr = value.toString();
							int pos = wrkStr.indexOf(".");
							if (pos >= 0) {
								wrkStr = wrkStr.substring(0, pos);
							}
							value_ = Integer.parseInt(wrkStr);
						}
					} else {
						if (basicType.equals("FLOAT")) {
							if (value == null || value.equals("")) {
								value_ = "";
							} else {
								value_ = Float.parseFloat(value.toString());
							}
						} else {
							//value_ = value;
							if (result == null) {
								value_ = "";
							} else {
								String stringValue = result.getString(this.getFieldID());
								if (stringValue == null) {
									value_ = "";
								} else {
									value_ = stringValue.trim();
								}
							}
						}
					}
//				}
			}
			//
			oldValue_ = value_;
			//
		} catch (SQLException e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	public void setValue(Object value){
		value_ = XFUtility.getValueAccordingToBasicType(this.getBasicType(), value);
	}

	public Object getValue() {
		return getInternalValue();
	}

	public void setOldValue(Object value){
		oldValue_ = XFUtility.getValueAccordingToBasicType(this.getBasicType(), value);
	}

	public Object getOldValue(){
		return oldValue_;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public boolean isNonEditableField() {
		return isNonEditableField;
	}

	public void setEditable(boolean editable) {
		if (editable && !isNonEditableField) {
			isEditable = true;
		} else {
			isEditable = false;
		}
	}

	public void setColor(String color) {
		foreground = XFUtility.convertStringToColor(color);
	}

	public String getColor() {
		return XFUtility.convertColorToString(foreground);
	}
}

class XF310_HeaderTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID = "";
	private String activeWhere = "";
	private String fixedWhere = "";
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<String> uniqueKeyList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF310 dialog_;
	private StringTokenizer workTokenizer;
	private String updateCounterID = "";
	private int updateCounterValue = 0;

	public XF310_HeaderTable(org.w3c.dom.Element functionElement, XF310 dialog){
		super();
		//
		functionElement_ = functionElement;
		dialog_ = dialog;
		//
		tableID = functionElement_.getAttribute("HeaderTable");
		tableElement = dialog_.getSession().getTableElement(tableID);
		activeWhere = tableElement.getAttribute("ActiveWhere");
		updateCounterID = tableElement.getAttribute("UpdateCounter");
		if (updateCounterID.equals("")) {
			updateCounterID = XFUtility.DEFAULT_UPDATE_COUNTER;
		}
		fixedWhere = functionElement_.getAttribute("HeaderFixedWhere");
		//
		String workString;
		org.w3c.dom.Element workElement;
		//
		if (functionElement_.getAttribute("HeaderKeyFields").equals("")) {
			NodeList nodeList = tableElement.getElementsByTagName("Key");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("Type").equals("PK")) {
					workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
					while (workTokenizer.hasMoreTokens()) {
						workString = workTokenizer.nextToken();
						keyFieldIDList.add(workString);
					}
					break;
				}
			}
		} else {
			workTokenizer = new StringTokenizer(functionElement_.getAttribute("HeaderKeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				keyFieldIDList.add(workTokenizer.nextToken());
			}
		}
		//
		NodeList nodeList = tableElement.getElementsByTagName("Key");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			if (workElement.getAttribute("Type").equals("SK")) {
				uniqueKeyList.add(workElement.getAttribute("Fields"));
			}
		}
		//
		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID, element));
		}
	}
	
	public void setUpdateCounterValue(ResultSet result) {
		try {
			updateCounterValue = result.getInt(updateCounterID);
		} catch (SQLException e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	public String getSQLToSelect(){
		int count;
		StringBuffer buf = new StringBuffer();
		//
		buf.append("select ");
		//
		count = -1;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isFieldOnPrimaryTable() && !dialog_.getHeaderFieldList().get(i).isVirtualField()) {
				count++;
				if (count > 0) {
					buf.append(",");
				}
				buf.append(dialog_.getHeaderFieldList().get(i).getFieldID());
			}
		}
		buf.append(",");
		buf.append(updateCounterID);
		//
		buf.append(" from ");
		buf.append(tableID);
		//
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isKey()) {
				count++;
				if (count > 0) {
					buf.append(" and ") ;
				}
				buf.append(dialog_.getHeaderFieldList().get(i).getFieldID()) ;
				buf.append("=") ;
				//if (dialog_.getHeaderFieldList().get(i).isLiteralRequired()) {
				if (XFUtility.isLiteralRequiredBasicType(dialog_.getHeaderFieldList().get(i).getBasicType())) {
					buf.append("'") ;
					buf.append(dialog_.getParmMap().get(dialog_.getHeaderFieldList().get(i).getFieldID()));
					buf.append("'") ;
				} else {
					buf.append(dialog_.getParmMap().get(dialog_.getHeaderFieldList().get(i).getFieldID()));
				}
			}
		}
		//
		if (!activeWhere.equals("")) {
			buf.append(" and (");
			buf.append(activeWhere);
			buf.append(") ");
		}
		//
		if (!fixedWhere.equals("")) {
			buf.append(" and (");
			buf.append(fixedWhere);
			buf.append(") ");
		}
		//
		return buf.toString();
	}

	String getSQLToUpdate() {
		StringBuffer statementBuf = new StringBuffer();
		//
		statementBuf.append("update ");
		statementBuf.append(tableID);
		statementBuf.append(" set ");
		//
		boolean firstField = true;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isFieldOnPrimaryTable()
			&& !dialog_.getHeaderFieldList().get(i).isKey()
			&& !dialog_.getHeaderFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ");
				}
				statementBuf.append(dialog_.getHeaderFieldList().get(i).getFieldID());
				statementBuf.append("=");
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(i).getBasicType(), dialog_.getHeaderFieldList().get(i).getInternalValue()));
				firstField = false;
			}
		}
		statementBuf.append(", ");
		statementBuf.append(updateCounterID);
		statementBuf.append("=");
		statementBuf.append(updateCounterValue + 1);
		//
		statementBuf.append(" where ") ;
		//
		firstField = true;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isKey()) {
				if (!firstField) {
					statementBuf.append(" and ") ;
				}
				statementBuf.append(dialog_.getHeaderFieldList().get(i).getFieldID());
				statementBuf.append("=");
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(i).getBasicType(), dialog_.getHeaderFieldList().get(i).getInternalValue()));
				firstField = false;
			}
		}
		statementBuf.append(" and ");
		statementBuf.append(updateCounterID);
		statementBuf.append("=");
		statementBuf.append(updateCounterValue);
		//
		return statementBuf.toString();
	}
	
	public String getSQLToCheckSKDuplication(ArrayList<String> keyFieldList) {
		StringBuffer statementBuf = new StringBuffer();
		//
		statementBuf.append("select ");
		//
		boolean firstField = true;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isFieldOnPrimaryTable()
					&& !dialog_.getHeaderFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(",");
				}
				statementBuf.append(dialog_.getHeaderFieldList().get(i).getFieldID());
				firstField = false;
			}
		}
		//
		statementBuf.append(" from ");
		statementBuf.append(tableID);
		statementBuf.append(" where ");
		//
		firstField = true;
		for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
			if (dialog_.getHeaderFieldList().get(j).isFieldOnPrimaryTable()) {
				for (int p = 0; p < keyFieldList.size(); p++) {
					if (dialog_.getHeaderFieldList().get(j).getFieldID().equals(keyFieldList.get(p))) {
						if (!firstField) {
							statementBuf.append(" and ");
						}
						statementBuf.append(dialog_.getHeaderFieldList().get(j).getFieldID());
						statementBuf.append("=");
						statementBuf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(j).getBasicType(), dialog_.getHeaderFieldList().get(j).getInternalValue()));
						firstField = false;
					}
				}
			}
		}
		//
		firstField = true;
		for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
			if (dialog_.getHeaderFieldList().get(j).isFieldOnPrimaryTable()) {
				if (dialog_.getHeaderFieldList().get(j).isKey()) {
					if (firstField) {
						statementBuf.append(" and (");
					} else {
						statementBuf.append(" or ");
					}
					statementBuf.append(dialog_.getHeaderFieldList().get(j).getFieldID());
					statementBuf.append("!=");
					statementBuf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(j).getBasicType(), dialog_.getHeaderFieldList().get(j).getInternalValue()));
					firstField = false;
				}
			}
		}
		statementBuf.append(")") ;
		//
		return statementBuf.toString();
	}
	
	boolean hasPrimaryKeyValueAltered() {
		boolean altered = false;
		//
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isKey()) {
				if (!dialog_.getHeaderFieldList().get(i).getValue().equals(dialog_.getHeaderFieldList().get(i).getOldValue())) {
					altered = true;
				}
			}
		}
		//
		return altered;
	}

	public String getTableID(){
		return tableID;
	}

	public org.w3c.dom.Element getTableElement(){
		return tableElement;
	}
	
	public ArrayList<String> getKeyFieldIDList(){
		return keyFieldIDList;
	}
	
	public ArrayList<String> getUniqueKeyList(){
		return uniqueKeyList;
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}
	
	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF310_HeaderReferTable referTable;
		org.w3c.dom.Element workElement;
		//
		if (this.getTableID().equals(tableID) && this.getTableID().equals(tableAlias)) {
			NodeList nodeList = tableElement.getElementsByTagName("Field");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("ID").equals(fieldID)) {
					isValid = true;
					break;
				}
			}
		} else {
			for (int i = 0; i < dialog_.getHeaderReferTableList().size(); i++) {
				referTable = dialog_.getHeaderReferTableList().get(i);
				if (referTable.getTableID().equals(tableID) && referTable.getTableAlias().equals(tableAlias)) {
					for (int j = 0; j < referTable.getFieldIDList().size(); j++) {
						if (referTable.getFieldIDList().get(j).equals(fieldID)) {
							isValid = true;
							break;
						}
					}
				}
				if (isValid) {
					break;
				}
			}
		}
		//
		return isValid;
	}

	public int runScript(String event1, String event2) throws ScriptException  {
		int countOfErrors = 0;
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		//
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		//
		if (validScriptList.size() > 0) {
			//
			for (int i = 0; i < validScriptList.size(); i++) {
				dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText(), null);
			}
			//
			for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
				if (dialog_.getHeaderFieldList().get(i).isError()) {
					countOfErrors++;
				}
			}
		}
		//
		return countOfErrors;
	}
}

class XF310_HeaderReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF310 dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private String activeWhere = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private boolean isOptional = false;
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private String rangeKeyFieldSearch = "";
	private boolean rangeValidated;

	public XF310_HeaderReferTable(org.w3c.dom.Element referElement, XF310 dialog){
		super();
		//
		referElement_ = referElement;
		dialog_ = dialog;
		//
		tableID = referElement_.getAttribute("ToTable");
		tableElement = dialog_.getSession().getTableElement(tableID);
		//
		StringTokenizer workTokenizer;
		String wrkStr = tableElement.getAttribute("RangeKey");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			rangeKeyFieldValid =workTokenizer.nextToken();
			rangeKeyFieldExpire =workTokenizer.nextToken();
			org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				rangeKeyType = 1;
			} else {
				rangeKeyType = 2;
			}
		}
		//
		activeWhere = tableElement.getAttribute("ActiveWhere");
		//
		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			fieldIDList.add(workTokenizer.nextToken());
		}
		//
		if (referElement_.getAttribute("ToKeyFields").equals("")) {
			org.w3c.dom.Element workElement = dialog.getSession().getTablePKElement(tableID);
			workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				toKeyFieldIDList.add(workTokenizer.nextToken());
			}
		} else {
			workTokenizer = new StringTokenizer(referElement_.getAttribute("ToKeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				toKeyFieldIDList.add(workTokenizer.nextToken());
			}
		}
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("WithKeyFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			withKeyFieldIDList.add(workTokenizer.nextToken());
		}
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			orderByFieldIDList.add(workTokenizer.nextToken());
		}
		//
		if (referElement_.getAttribute("Optional").equals("T")) {
			isOptional = true;
		}
	}

	public String getSelectSQL(boolean isToGetRecordsForComboBox){
		//
		int count;
		StringBuffer buf = new StringBuffer();
		//
		buf.append("select ");
		//
		org.w3c.dom.Element workElement;
		count = 0;
		for (int i = 0; i < fieldIDList.size(); i++) {
			workElement = dialog_.getSession().getFieldElement(tableID, fieldIDList.get(i));
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				if (count > 0) {
					buf.append(",");
				}
				count++;
				buf.append(fieldIDList.get(i));
			}
		}
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (count > 0) {
				buf.append(",");
			}
			count++;
			buf.append(toKeyFieldIDList.get(i));
		}
		if (!rangeKeyFieldValid.equals("")) {
			if (count > 0) {
				buf.append(",");
			}
			buf.append(rangeKeyFieldValid);
			//
			workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				buf.append(",");
				buf.append(rangeKeyFieldExpire);
			}
		}
		//
		buf.append(" from ");
		buf.append(tableID);
		//
		StringTokenizer workTokenizer;
		String keyFieldID, keyFieldTableID;
		count = 0;
		boolean isToBeWithValue;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				rangeKeyFieldSearch = withKeyFieldIDList.get(i);
			} else {
				if (isToGetRecordsForComboBox) {
					//
					// Value of the field which has either of these conditions should be within WHERE to SELECT records: //
					// 1. The with-key-field is not editable //
					// 2. The with-key-field is part of PK of the header table //
					// 3. The with-key-field is on the header table and consists of upper part of with-key-fields //
					// 4. The with-key-field is part of PK of the other join table //
					for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getHeaderFieldList().get(j).getDataSourceName())) {
							//
							isToBeWithValue = false;
							//
							if (!dialog_.getHeaderFieldList().get(j).isEditable()) {
								isToBeWithValue = true;
							} else {
								workTokenizer = new StringTokenizer(withKeyFieldIDList.get(i), "." );
								keyFieldTableID = workTokenizer.nextToken();
								keyFieldID = workTokenizer.nextToken();
								if (keyFieldTableID.equals(dialog_.getHeaderTable().getTableID())) {
									if (withKeyFieldIDList.size() > 1 && i < (withKeyFieldIDList.size() - 1)) {
										isToBeWithValue = true;
									} else {
										for (int k = 0; k < dialog_.getKeyFieldList().size(); k++) {
											if (keyFieldID.equals(dialog_.getKeyFieldList().get(k))) {
												isToBeWithValue = true;
											}
										}
									}
								} else {
									if (!keyFieldTableID.equals(this.tableAlias)) {
										isToBeWithValue = true;
									}
								}
							}
							//
							if (isToBeWithValue) {
								if (count == 0) {
									buf.append(" where ");
								} else {
									buf.append(" and ");
								}
								buf.append(toKeyFieldIDList.get(i));
								buf.append("=");
								//buf.append(dialog_.getHeaderFieldList().get(j).getTableOperationValue());
								buf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(j).getBasicType(), dialog_.getHeaderFieldList().get(j).getInternalValue()));
								count++;
								break;
							}
						}
					}
				} else {
					if (count == 0) {
						buf.append(" where ");
					} else {
						buf.append(" and ");
					}
					buf.append(toKeyFieldIDList.get(i));
					buf.append("=");
					for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
							//if (dialog_.getHeaderFieldList().get(j).isLiteralRequired()) {
							if (XFUtility.isLiteralRequiredBasicType(dialog_.getHeaderFieldList().get(j).getBasicType())) {
								buf.append("'") ;
								buf.append(dialog_.getHeaderFieldList().get(j).getInternalValue());
								buf.append("'") ;
							} else {
								buf.append(dialog_.getHeaderFieldList().get(j).getInternalValue());
							}
							break;
						}
					}
					count++;
				}
			}
		}
		//
		if (!activeWhere.equals("")) {
			buf.append(" and ");
			buf.append(activeWhere);
		}
		//
		if (this.rangeKeyType != 0) {
			buf.append(" order by ");
			buf.append(rangeKeyFieldValid);
			buf.append(" DESC ");
		} else {
			if (orderByFieldIDList.size() > 0) {
				int pos0,pos1;
				buf.append(" order by ");
				for (int i = 0; i < orderByFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					pos0 = orderByFieldIDList.get(i).indexOf(".");
					pos1 = orderByFieldIDList.get(i).indexOf("(A)");
					if (pos1 >= 0) {
						buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
					} else {
						pos1 = orderByFieldIDList.get(i).indexOf("(D)");
						if (pos1 >= 0) {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
							buf.append(" DESC ");
						} else {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, orderByFieldIDList.get(i).length()));
						}
					}
				}
			}
		}
		//
		rangeValidated = false;
		//
		return buf.toString();
	}

	public String getTableID(){
		return tableID;
	}
	
	public String getTableAlias(){
		return tableAlias;
	}
	
	public boolean isOptional() {
		return isOptional;
	}

	public ArrayList<String> getKeyFieldIDList(){
		return toKeyFieldIDList;
	}
	
	public ArrayList<String> getWithKeyFieldIDList(){
		return withKeyFieldIDList;
	}

	public ArrayList<String> getFieldIDList(){
		return  fieldIDList;
	}
	
	public void setToBeExecuted(boolean executed){
		isToBeExecuted = executed;
	}
	
	public boolean isToBeExecuted(){
		return isToBeExecuted;
	}

	public boolean isKeyNullable() {
		boolean isKeyNullable = false;
		//
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
					if (dialog_.getHeaderFieldList().get(j).isNullable()) {
						isKeyNullable = true;
						break;
					}
				}
			}
		}
		//
		return isKeyNullable;
	}

	public boolean isKeyNull() {
		boolean isKeyNull = false;
		//
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
					if (dialog_.getHeaderFieldList().get(j).isNull()) {
						isKeyNull = true;
						break;
					}
				}
			}
		}
		//
		return isKeyNull;
	}
	
	public boolean isRecordToBeSelected(ResultSet result){
		boolean returnValue = false;
		//
		if (rangeKeyType == 0) {
			returnValue = true;
		}
		//
		// VIRTUAL SEARCH //
		if (rangeKeyType == 1) {
			try {
				if (!rangeValidated) { 
					// Note that result set is ordered by rangeKeyFieldValue DESC //
					Object valueKey = dialog_.getValueOfHeaderFieldByName(rangeKeyFieldSearch);
					Object valueFrom = result.getObject(rangeKeyFieldValid);
					int comp1 = valueKey.toString().compareTo(valueFrom.toString());
					if (comp1 >= 0) {
						returnValue = true;
						rangeValidated = true;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
		//
		// PHYSICAL SEARCH //
		if (rangeKeyType == 2) {
			try {
				Object valueKey = dialog_.getValueOfHeaderFieldByName(rangeKeyFieldSearch);
				Object valueFrom = result.getObject(rangeKeyFieldValid);
				Object valueThru = result.getObject(rangeKeyFieldExpire);
				if (valueThru == null) {
					valueThru = "9999-12-31";
				}
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				int comp2 = valueKey.toString().compareTo(valueThru.toString());
				if (comp1 >= 0 && comp2 < 0) {
					returnValue = true;
				}
			} catch (SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
		//
		return returnValue;
	}

	public void setErrorOnRelatedFields() {
		boolean noneOfKeyFieldsWereSetError = true;
		//
		/////////////////////////////////////////////////
		// Set error on the visible editable key field //
		/////////////////////////////////////////////////
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
				if (dialog_.getHeaderFieldList().get(j).isVisibleOnPanel()
						&& dialog_.getHeaderFieldList().get(j).isEditable()
						&& dialog_.getHeaderFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
						&& !dialog_.getHeaderFieldList().get(j).isError()) {
					//dialog_.getHeaderFieldList().get(j).setError(dialog_.getHeaderFieldList().get(j).getCaption() + res.getString("Colon") + tableElement.getAttribute("Name") + res.getString("FunctionError45"));
					dialog_.getHeaderFieldList().get(j).setError(tableElement.getAttribute("Name") + res.getString("FunctionError45"));
					//dialog_.getMessageList().add(dialog_.getHeaderFieldList().get(j).getError());
					noneOfKeyFieldsWereSetError = false;
					break;
				}
			}
		}
		//
		if (noneOfKeyFieldsWereSetError) {
			//
			///////////////////////////////////////////////////////
			// Set error on the visible editable attribute field //
			///////////////////////////////////////////////////////
			for (int i = 0; i < fieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
					if (dialog_.getHeaderFieldList().get(j).isVisibleOnPanel()
							&& dialog_.getHeaderFieldList().get(j).isEditable()
							&& dialog_.getHeaderFieldList().get(j).getFieldID().equals(fieldIDList.get(i))
							&& dialog_.getHeaderFieldList().get(j).getTableAlias().equals(this.tableAlias)
							&& !dialog_.getHeaderFieldList().get(j).isError()) {
						//dialog_.getHeaderFieldList().get(j).setError(dialog_.getHeaderFieldList().get(j).getCaption() + res.getString("Colon") + tableElement.getAttribute("Name") + res.getString("FunctionError45"));
						dialog_.getHeaderFieldList().get(j).setError(tableElement.getAttribute("Name") + res.getString("FunctionError45"));
						//dialog_.getMessageList().add(dialog_.getHeaderFieldList().get(j).getError());
						noneOfKeyFieldsWereSetError = false;
						break;
					}
				}
			}
		}
		//
		if (noneOfKeyFieldsWereSetError) {
			//
			//////////////////////////////////////
			// Set error on the first key field //
			//////////////////////////////////////
			for (int i = 0; i < toKeyFieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
					if (dialog_.getHeaderFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
							&& !dialog_.getHeaderFieldList().get(j).isError()) {
						//dialog_.getHeaderFieldList().get(j).setError(dialog_.getHeaderFieldList().get(j).getCaption() + res.getString("Colon") + tableElement.getAttribute("Name") + res.getString("FunctionError45"));
						dialog_.getHeaderFieldList().get(j).setError(tableElement.getAttribute("Name") + res.getString("FunctionError45"));
						//dialog_.getMessageList().add(dialog_.getHeaderFieldList().get(j).getError());
						break;
					}
				}
			}
		}
	}
	//
	public void setKeyFieldValues(XFHashMap keyValues){
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
				if (dialog_.getHeaderFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))) {
					dialog_.getHeaderFieldList().get(j).setValue(keyValues.getValue(withKeyFieldIDList.get(i)));
					break;
				}
			}
		}
	}
}

class XF310_DetailTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID_ = "";
	private String activeWhere = "";
	private String updateValueToInactivate = "";
	private String fixedWhere = "";
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<String> uniqueKeyList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF310 dialog_;
	private StringTokenizer workTokenizer;
	private boolean hasOrderByAsItsOwnFields = true;
	private String detailRowNoID = "";
	private String updateCounterID = "";

	public XF310_DetailTable(org.w3c.dom.Element functionElement, XF310 dialog){
		super();
		//
		functionElement_ = functionElement;
		dialog_ = dialog;
		//
		tableID_ = functionElement_.getAttribute("DetailTable");
		tableElement = dialog_.getSession().getTableElement(tableID_);
		activeWhere = tableElement.getAttribute("ActiveWhere");
		updateValueToInactivate = tableElement.getAttribute("DeleteOperation");
		updateCounterID = tableElement.getAttribute("UpdateCounter");
		if (updateCounterID.equals("")) {
			updateCounterID = XFUtility.DEFAULT_UPDATE_COUNTER;
		}
		fixedWhere = functionElement_.getAttribute("DetailFixedWhere");
		//
		int pos1;
		String wrkStr1, wrkStr2;
		org.w3c.dom.Element workElement, fieldElement;
		//
		if (functionElement_.getAttribute("DetailKeyFields").equals("")) {
			NodeList nodeList = tableElement.getElementsByTagName("Key");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("Type").equals("PK")) {
					workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
					while (workTokenizer.hasMoreTokens()) {
						wrkStr1 = workTokenizer.nextToken();
						keyFieldIDList.add(wrkStr1);
					}
					break;
				}
			}
		} else {
			workTokenizer = new StringTokenizer(functionElement_.getAttribute("DetailKeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				keyFieldIDList.add(workTokenizer.nextToken());
			}
		}
		//
		NodeList nodeList = tableElement.getElementsByTagName("Key");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			if (workElement.getAttribute("Type").equals("PK")) {
				wrkStr1 = "";
				workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
				if (workTokenizer.countTokens() > 1) {
					while (workTokenizer.hasMoreTokens()) {
						wrkStr1 = workTokenizer.nextToken();
					}
					if (!wrkStr1.equals("")) {
						fieldElement = dialog_.getSession().getFieldElement(tableID_, wrkStr1);
						if (XFUtility.getBasicTypeOf(fieldElement.getAttribute("Type")).equals("INTEGER")
								&& tableElement.getAttribute("DetailRowNumberAuto").equals("T")) {
							detailRowNoID = wrkStr1;
						}
					}
				}
			}
			if (workElement.getAttribute("Type").equals("SK")) {
				uniqueKeyList.add(workElement.getAttribute("Fields"));
			}
		}
		//
		workTokenizer = new StringTokenizer(functionElement_.getAttribute("DetailOrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			wrkStr1 = workTokenizer.nextToken();
			//
			pos1 = wrkStr1.indexOf(".");
			if (pos1 > -1) { 
				wrkStr2 = wrkStr1.substring(0, pos1);
				if (!wrkStr2.equals(tableID_)) {
					hasOrderByAsItsOwnFields = false;
				}
			}
			//
			orderByFieldIDList.add(wrkStr1);
		}
		//
		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID_, element));
		}
	}
	
	public String getDetailRowNoID() {
		return detailRowNoID;
	}
	
	public String getSQLToSelect(){
		int count;
		StringBuffer buf = new StringBuffer();
		XF310_HeaderField headerField;
		//
		buf.append("select ");
		//
		count = -1;
		for (int i = 0; i < keyFieldIDList.size(); i++) {
			count++;
			if (count > 0) {
				buf.append(",");
			}
			buf.append(keyFieldIDList.get(i));
		}
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).getTableID().equals(tableID_) && !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				count++;
				if (count > 0) {
					buf.append(",");
				}
				buf.append(dialog_.getDetailColumnList().get(i).getFieldID());
			}
		}
		if (count > 0) {
			buf.append(",");
		}
		buf.append(updateCounterID);
		//
		buf.append(" from ");
		buf.append(tableID_);
		//
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < dialog_.getHeaderTable().getKeyFieldIDList().size(); i++) {
			count++;
			if (count > 0) {
				buf.append(" and ") ;
			}
			buf.append(keyFieldIDList.get(i)) ;
			buf.append("=") ;
			headerField = dialog_.getHeaderFieldObjectByID(dialog_.getHeaderTable().getTableID(), "", dialog_.getHeaderTable().getKeyFieldIDList().get(i));
			if (XFUtility.isLiteralRequiredBasicType(headerField.getBasicType())) {
				buf.append("'");
				buf.append(dialog_.getParmMap().get(dialog_.getHeaderTable().getKeyFieldIDList().get(i)));
				buf.append("'");
			} else {
				buf.append(dialog_.getParmMap().get(dialog_.getHeaderTable().getKeyFieldIDList().get(i)));
			}
		}
		//
		if (!activeWhere.equals("")) {
			buf.append(" and (");
			buf.append(activeWhere);
			buf.append(") ");
		}
		//
		if (!fixedWhere.equals("")) {
			buf.append(" and (");
			buf.append(fixedWhere);
			buf.append(") ");
		}
		//
		if (this.hasOrderByAsItsOwnFields) {
			if (orderByFieldIDList.size() > 0) {
				int pos0,pos1;
				buf.append(" order by ");
				for (int i = 0; i < orderByFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					pos0 = orderByFieldIDList.get(i).indexOf(".");
					pos1 = orderByFieldIDList.get(i).indexOf("(A)");
					if (pos1 >= 0) {
						buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
					} else {
						pos1 = orderByFieldIDList.get(i).indexOf("(D)");
						if (pos1 >= 0) {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
							buf.append(" DESC ");
						} else {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, orderByFieldIDList.get(i).length()));
						}
					}
				}
			} else {
				buf.append(" order by ");
				for (int i = 0; i < keyFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					buf.append(keyFieldIDList.get(i));
				}
			}
		}
		//
		return buf.toString();
	}

	String getSQLToUpdate(XF310_DetailRowNumber rowNumber) {
		StringBuffer statementBuf = new StringBuffer();
		//
		statementBuf.append("update ");
		statementBuf.append(tableID_);
		statementBuf.append(" set ");
		//
		boolean firstField = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isFieldOnDetailTable()
			&& !dialog_.getDetailColumnList().get(i).isKey()
			&& !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ");
				}
				statementBuf.append(dialog_.getDetailColumnList().get(i).getFieldID());
				statementBuf.append("=");
				statementBuf.append(convertToTableOperationValue(dialog_.getDetailColumnList().get(i).getBasicType(), rowNumber.getColumnValueMap().get(dialog_.getDetailColumnList().get(i).getDataSourceName())));
				firstField = false;
			}
		}
		statementBuf.append(", ");
		statementBuf.append(updateCounterID);
		statementBuf.append("=");
		statementBuf.append((Integer)rowNumber.getColumnValueMap().get(updateCounterID) + 1);
		//
		statementBuf.append(" where ") ;
		//
		boolean firstKey = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			for (int j = 0; j < keyFieldIDList.size(); j++) {
				if (dialog_.getDetailColumnList().get(i).getTableID().equals(tableID_) && 
						dialog_.getDetailColumnList().get(i).getFieldID().equals(keyFieldIDList.get(j))) {
					if (!firstKey) {
						statementBuf.append(" and ") ;
					}
					statementBuf.append(keyFieldIDList.get(j)) ;
					statementBuf.append("=") ;
					statementBuf.append(convertToTableOperationValue(dialog_.getDetailColumnList().get(i).getBasicType(), rowNumber.getKeyValueMap().get(keyFieldIDList.get(j))));
					firstKey = false;
				}
			}
		}
		statementBuf.append(" and ") ;
		statementBuf.append(updateCounterID) ;
		statementBuf.append("=") ;
		statementBuf.append(rowNumber.getColumnValueMap().get(updateCounterID));
		//
		return statementBuf.toString();
	}

	String getSQLToInsert(XF310_DetailRowNumber rowNumber) {
		StringBuffer statementBuf = new StringBuffer();
		//
		statementBuf.append("insert into ");
		statementBuf.append(tableID_);
		statementBuf.append(" (");
		//
		boolean firstField = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isFieldOnDetailTable()
			&& !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ");
				}
				statementBuf.append(dialog_.getDetailColumnList().get(i).getFieldID());
				firstField = false;
			}
		}
		//
		statementBuf.append(") values(") ;
		//
		firstField = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isFieldOnDetailTable()
			&& !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ");
				}
				if (dialog_.getDetailColumnList().get(i).isKey()) {
					statementBuf.append(convertToTableOperationValue(dialog_.getDetailColumnList().get(i).getBasicType(), rowNumber.getKeyValueMap().get(dialog_.getDetailColumnList().get(i).getFieldID())));
				} else {
					statementBuf.append(convertToTableOperationValue(dialog_.getDetailColumnList().get(i).getBasicType(), rowNumber.getColumnValueMap().get(dialog_.getDetailColumnList().get(i).getDataSourceName())));
				}
				firstField = false;
			}
		}
		//
		statementBuf.append(")") ;
		//
		return statementBuf.toString();
	}

	String getSQLToDelete(HashMap<String, Object> keyMap, int updateCounterValue) {
		StringBuffer statementBuf = new StringBuffer();
		//
		if (updateValueToInactivate.equals("")) {
			statementBuf.append("delete from ");
			statementBuf.append(tableID_);
		} else {
			statementBuf.append("update ");
			statementBuf.append(tableID_);
			statementBuf.append(" set ");
			statementBuf.append(updateValueToInactivate) ;
			statementBuf.append(", ") ;
			statementBuf.append(updateCounterID) ;
			statementBuf.append("=") ;
			statementBuf.append(updateCounterValue + 1) ;
		}
		//
		statementBuf.append(" where ") ;
		//
		boolean firstKey = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			for (int j = 0; j < keyFieldIDList.size(); j++) {
				if (dialog_.getDetailColumnList().get(i).getTableID().equals(tableID_) && 
						dialog_.getDetailColumnList().get(i).getFieldID().equals(keyFieldIDList.get(j))) {
					if (!firstKey) {
						statementBuf.append(" and ") ;
					}
					statementBuf.append(keyFieldIDList.get(j)) ;
					statementBuf.append("=") ;
					if (XFUtility.isLiteralRequiredBasicType(dialog_.getDetailColumnList().get(i).getBasicType())) {
						statementBuf.append("'");
						statementBuf.append(keyMap.get(keyFieldIDList.get(j))) ;
						statementBuf.append("'");
					} else {
						statementBuf.append(keyMap.get(keyFieldIDList.get(j))) ;
					}
					firstKey = false;
				}
			}
		}
		statementBuf.append(" and ") ;
		statementBuf.append(updateCounterID) ;
		statementBuf.append("=") ;
		statementBuf.append(updateCounterValue) ;
		//
		return statementBuf.toString();
	}
	
	public String getSQLToCheckSKDuplication(XF310_DetailRowNumber rowNumber, ArrayList<String> keyFieldList, boolean isToUpdate) {
		StringBuffer statementBuf = new StringBuffer();
		//
		statementBuf.append("select ");
		//
		boolean firstField = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isFieldOnDetailTable()
					&& !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(",");
				}
				statementBuf.append(dialog_.getDetailColumnList().get(i).getFieldID());
				firstField = false;
			}
		}
		//
		statementBuf.append(" from ");
		statementBuf.append(tableID_);
		statementBuf.append(" where ") ;
		//
		firstField = true;
		for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
			if (dialog_.getDetailColumnList().get(j).isFieldOnDetailTable()) {
				for (int p = 0; p < keyFieldList.size(); p++) {
					if (dialog_.getDetailColumnList().get(j).getFieldID().equals(keyFieldList.get(p))) {
						if (!firstField) {
							statementBuf.append(" and ") ;
						}
						statementBuf.append(dialog_.getDetailColumnList().get(j).getFieldID()) ;
						statementBuf.append("=") ;
						statementBuf.append(convertToTableOperationValue(dialog_.getDetailColumnList().get(j).getBasicType(), rowNumber.getKeyValueMap().get(dialog_.getDetailColumnList().get(j).getFieldID())));
						firstField = false;
					}
				}
			}
		}
		//
		if (isToUpdate) {
			firstField = true;
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (dialog_.getDetailColumnList().get(j).isFieldOnDetailTable()) {
					if (dialog_.getDetailColumnList().get(j).isKey()) {
						if (firstField) {
							statementBuf.append(" and (") ;
						} else {
							statementBuf.append(" or ") ;
						}
						statementBuf.append(dialog_.getDetailColumnList().get(j).getFieldID()) ;
						statementBuf.append("!=") ;
						statementBuf.append(convertToTableOperationValue(dialog_.getDetailColumnList().get(j).getBasicType(), rowNumber.getKeyValueMap().get(dialog_.getDetailColumnList().get(j).getFieldID())));
						firstField = false;
					}
				}
			}
			statementBuf.append(")") ;
		}
		//
		return statementBuf.toString();
	}

	public Object convertToTableOperationValue(String basicType, Object value){
		Object returnValue = null;
		if (basicType.equals("INTEGER")) {
			String wrkStr = value.toString().replace(".0", "");
			returnValue = Integer.parseInt(wrkStr);
		}
		if (basicType.equals("FLOAT")) {
			returnValue = Double.parseDouble(value.toString());
		}
		if (basicType.equals("STRING")) {
			returnValue = "'" + (String)value + "'";
		}
		if (basicType.equals("DATE")) {
			if (value == null || value.equals("")) {
				returnValue = "NULL";
			} else {
				returnValue = "'" + (String)value + "'";
			}
		}
		if (basicType.equals("DATETIME")) {
			String timeDate = (String)value;
			if (timeDate == null || timeDate.equals("")) {
				returnValue = "NULL";
			} else {
				if (timeDate.equals("CURRENT_TIMESTAMP")) {
					returnValue = timeDate;
				} else {
					timeDate = timeDate.replace("/", "-");
					returnValue = "'" + timeDate + "'";
				}
			}
		}
		return returnValue;
	}

	public String getTableID(){
		return tableID_;
	}
	
	public String getUpdateCounterID(){
		return updateCounterID;
	}
	
	public ArrayList<String> getKeyFieldIDList(){
		return keyFieldIDList;
	}
	
	public ArrayList<String> getUniqueKeyList(){
		return uniqueKeyList;
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}

	public org.w3c.dom.Element getTableElement() {
		return tableElement;
	}
	
	public ArrayList<String> getOrderByFieldIDList(){
		return orderByFieldIDList;
	}
	
	public boolean hasOrderByAsItsOwnFields(){
		return hasOrderByAsItsOwnFields;
	}
	
	boolean hasPrimaryKeyValueAltered(XF310_DetailRowNumber rowNumber) {
		boolean altered = false;
		//
		for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
			if (dialog_.getDetailColumnList().get(j).isKey()) {
				if (!rowNumber.getColumnValueMap().get(dialog_.getDetailColumnList().get(j).getDataSourceName()).equals(rowNumber.getColumnOldValueMap().get(dialog_.getDetailColumnList().get(j).getDataSourceName()))) {
					altered = true;
				}
			}
		}
		//
		return altered;
	}

	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF310_DetailReferTable referTable;
		org.w3c.dom.Element workElement;
		//
		if (this.getTableID().equals(tableID) && this.getTableID().equals(tableAlias)) {
			NodeList nodeList = tableElement.getElementsByTagName("Field");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("ID").equals(fieldID)) {
					isValid = true;
					break;
				}
			}
		} else {
			for (int i = 0; i < dialog_.getDetailReferTableList().size(); i++) {
				referTable = dialog_.getDetailReferTableList().get(i);
				if (referTable.getTableID().equals(tableID) && referTable.getTableAlias().equals(tableAlias)) {
					for (int j = 0; j < referTable.getFieldIDList().size(); j++) {
						if (referTable.getFieldIDList().get(j).equals(fieldID)) {
							isValid = true;
							break;
						}
					}
				}
				if (isValid) {
					break;
				}
			}
		}
		//
		return isValid;
	}

	public int runScript(String event1, String event2, HashMap<String, Object> columnValueMap, HashMap<String, Object> columnOldValueMap) throws ScriptException  {
		int countOfErrors = 0;
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		//
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		//
		if (validScriptList.size() > 0) {
			//
			for (int i = 0; i < validScriptList.size(); i++) {
				dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText(), null);
			}
			//
			for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
				if (columnValueMap != null) {
					columnValueMap.put(dialog_.getDetailColumnList().get(i).getDataSourceName(), dialog_.getDetailColumnList().get(i).getInternalValue());
				}
				if (columnOldValueMap != null) {
					columnOldValueMap.put(dialog_.getDetailColumnList().get(i).getDataSourceName(), dialog_.getDetailColumnList().get(i).getOldValue());
				}
				if (dialog_.getDetailColumnList().get(i).isError()) {
					countOfErrors++;
				}
			}
		}
		//
		return countOfErrors;
	}
}

class XF310_DetailReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF310 dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private String activeWhere = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private boolean isOptional = false;
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private String rangeKeyFieldSearch = "";
	private boolean rangeValidated;

	public XF310_DetailReferTable(org.w3c.dom.Element referElement, XF310 dialog){
		super();
		//
		referElement_ = referElement;
		dialog_ = dialog;
		//
		tableID = referElement_.getAttribute("ToTable");
		tableElement = dialog_.getSession().getTableElement(tableID);
		//
		StringTokenizer workTokenizer;
		String wrkStr = tableElement.getAttribute("RangeKey");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			rangeKeyFieldValid =workTokenizer.nextToken();
			rangeKeyFieldExpire =workTokenizer.nextToken();
			org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				rangeKeyType = 1;
			} else {
				rangeKeyType = 2;
			}
		}
		//
		activeWhere = tableElement.getAttribute("ActiveWhere");
		//
		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			fieldIDList.add(workTokenizer.nextToken());
		}
		//
		if (referElement_.getAttribute("ToKeyFields").equals("")) {
			org.w3c.dom.Element workElement = dialog.getSession().getTablePKElement(tableID);
			workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				toKeyFieldIDList.add(workTokenizer.nextToken());
			}
		} else {
			workTokenizer = new StringTokenizer(referElement_.getAttribute("ToKeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				toKeyFieldIDList.add(workTokenizer.nextToken());
			}
		}
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("WithKeyFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			withKeyFieldIDList.add(workTokenizer.nextToken());
		}
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			orderByFieldIDList.add(workTokenizer.nextToken());
		}
		//
		if (referElement_.getAttribute("Optional").equals("T")) {
			isOptional = true;
		}
	}

	public String getSelectSQL(boolean isToGetRecordsForComboBox){
		int count;
		org.w3c.dom.Element workElement;
		StringBuffer buf = new StringBuffer();
		boolean validWhereKeys = false;
		//
		buf.append("select ");
		//
		count = 0;
		for (int i = 0; i < fieldIDList.size(); i++) {
			workElement = dialog_.getSession().getFieldElement(tableID, fieldIDList.get(i));
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				if (count > 0) {
					buf.append(",");
				}
				count++;
				buf.append(fieldIDList.get(i));
			}
		}
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (count > 0) {
				buf.append(",");
			}
			count++;
			buf.append(toKeyFieldIDList.get(i));
		}
		if (!rangeKeyFieldValid.equals("")) {
			if (count > 0) {
				buf.append(",");
			}
			buf.append(rangeKeyFieldValid);
			//
			workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				buf.append(",");
				buf.append(rangeKeyFieldExpire);
			}
		}
		//
		buf.append(" from ");
		buf.append(tableID);
		//
		StringTokenizer workTokenizer;
		String keyFieldID, keyFieldTableID;
		count = 0;
		boolean isToBeWithValue;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				rangeKeyFieldSearch = withKeyFieldIDList.get(i);
			} else {
				if (isToGetRecordsForComboBox) {
					//
					// Value of the field which has either of these conditions should be within WHERE to SELECT records: //
					// 1. The with-key-field is not editable //
					// 2. The with-key-field is part of PK of the detail table //
					// 3. The with-key-field is on the detail table and consists of upper part of with-key-fields //
					// 4. The with-key-field is part of PK of the other join table //
					for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getDataSourceName())) {
							//
							isToBeWithValue = false;
							//
							if (!dialog_.getDetailColumnList().get(j).isEditable()) {
								isToBeWithValue = true;
							} else {
								workTokenizer = new StringTokenizer(withKeyFieldIDList.get(i), "." );
								keyFieldTableID = workTokenizer.nextToken();
								keyFieldID = workTokenizer.nextToken();
								if (keyFieldTableID.equals(dialog_.getDetailTable().getTableID())) {
									if (withKeyFieldIDList.size() > 1 && i < (withKeyFieldIDList.size() - 1)) {
										isToBeWithValue = true;
									} else {
										for (int k = 0; k < dialog_.getKeyFieldList().size(); k++) {
											if (keyFieldID.equals(dialog_.getKeyFieldList().get(k))) {
												isToBeWithValue = true;
											}
										}
									}
								} else {
									if (!keyFieldTableID.equals(this.tableAlias)) {
										isToBeWithValue = true;
									}
								}
							}
							//
							if (isToBeWithValue) {
								if (count == 0) {
									buf.append(" where ");
								} else {
									buf.append(" and ");
								}
								buf.append(toKeyFieldIDList.get(i));
								buf.append("=");
								buf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(j).getBasicType(), dialog_.getDetailColumnList().get(j).getInternalValue())) ;
								count++;
								break;
							}
						}
					}
				} else {
					if (count == 0) {
						buf.append(" where ");
					} else {
						buf.append(" and ");
					}
					buf.append(toKeyFieldIDList.get(i));
					buf.append("=");
					for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getDataSourceName())) {
							if (XFUtility.isLiteralRequiredBasicType(dialog_.getDetailColumnList().get(j).getBasicType())) {
								buf.append("'");
								buf.append(dialog_.getDetailColumnList().get(j).getInternalValue());
								buf.append("'");
								if (!dialog_.getDetailColumnList().get(j).getInternalValue().equals("")) {
									validWhereKeys = true;
								}
							} else {
								buf.append(dialog_.getDetailColumnList().get(j).getInternalValue());
								validWhereKeys = true;
							}
							break;
						}
					}
					count++;
				}
			}
		}
		//
		if (!activeWhere.equals("")) {
			buf.append(" and ");
			buf.append(activeWhere);
		}
		//
		if (this.rangeKeyType != 0) {
			buf.append(" order by ");
			buf.append(rangeKeyFieldValid);
			buf.append(" DESC ");
		} else {
			if (orderByFieldIDList.size() > 0) {
				int pos0,pos1;
				buf.append(" order by ");
				for (int i = 0; i < orderByFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					pos0 = orderByFieldIDList.get(i).indexOf(".");
					pos1 = orderByFieldIDList.get(i).indexOf("(A)");
					if (pos1 >= 0) {
						buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
					} else {
						pos1 = orderByFieldIDList.get(i).indexOf("(D)");
						if (pos1 >= 0) {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
							buf.append(" DESC ");
						} else {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, orderByFieldIDList.get(i).length()));
						}
					}
				}
			}
		}
		//
		rangeValidated = false;
		//
		if (isToGetRecordsForComboBox) {
			return buf.toString();
		} else {	
			if (validWhereKeys) {
				return buf.toString();
			} else {
				return "";
			}
		}
	}

	public String getTableID(){
		return tableID;
	}

	public String getTableAlias(){
		return tableAlias;
	}
	
	public boolean isOptional() {
		return isOptional;
	}
	
	public ArrayList<String> getKeyFieldIDList(){
		return toKeyFieldIDList;
	}
	
	public ArrayList<String> getWithKeyFieldIDList(){
		return withKeyFieldIDList;
	}

	public ArrayList<String> getFieldIDList(){
		return  fieldIDList;
	}
	
	public void setToBeExecuted(boolean executed){
		isToBeExecuted = executed;
	}
	
	public boolean isToBeExecuted(){
		return isToBeExecuted;
	}

	public boolean isKeyNullable() {
		boolean isKeyNullable = false;
		//
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getTableAlias() + "." + dialog_.getDetailColumnList().get(j).getFieldID())) {
					if (dialog_.getDetailColumnList().get(j).isNullable()) {
						isKeyNullable = true;
						break;
					}
				}
			}
		}
		//
		return isKeyNullable;
	}

	public boolean isKeyNull() {
		boolean isKeyNull = false;
		//
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getTableAlias() + "." + dialog_.getDetailColumnList().get(j).getFieldID())) {
					if (dialog_.getDetailColumnList().get(j).isNull()) {
						isKeyNull = true;
						break;
					}
				}
			}
		}
		//
		return isKeyNull;
	}
	
	public boolean isRecordToBeSelected(ResultSet result){
		boolean returnValue = false;
		//
		if (rangeKeyType == 0) {
			returnValue = true;
		}
		//
		if (rangeKeyType == 1) {
			try {
				// Note that result set is ordered by rangeKeyFieldValue DESC //
				if (!rangeValidated) { 
					StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
					String workTableAlias = workTokenizer.nextToken();
					String workFieldID = workTokenizer.nextToken();
					Object valueKey = dialog_.getDetailColumnObjectByID("", workTableAlias, workFieldID).getInternalValue();
					Object valueFrom = result.getObject(rangeKeyFieldValid);
					int comp1 = valueKey.toString().compareTo(valueFrom.toString());
					if (comp1 >= 0) {
						returnValue = true;
						rangeValidated = true;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
		//
		if (rangeKeyType == 2) {
			try {
				StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
				String workTableAlias = workTokenizer.nextToken();
				String workFieldID = workTokenizer.nextToken();
				Object valueKey = dialog_.getDetailColumnObjectByID("", workTableAlias, workFieldID).getInternalValue();
				Object valueFrom = result.getObject(rangeKeyFieldValid);
				Object valueThru = result.getObject(rangeKeyFieldExpire);
				if (valueThru == null) {
					int comp1 = valueKey.toString().compareTo(valueFrom.toString());
					if (comp1 >= 0) {
						returnValue = true;
					}
				} else {
					int comp1 = valueKey.toString().compareTo(valueFrom.toString());
					int comp2 = valueKey.toString().compareTo(valueThru.toString());
					if (comp1 >= 0 && comp2 < 0) {
						returnValue = true;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
		//
		return returnValue;
	}

	public void setKeyFieldValues(XFHashMap keyValues){
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (dialog_.getDetailColumnList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))) {
					dialog_.getDetailColumnList().get(j).setValue(keyValues.getValue(toKeyFieldIDList.get(i)));
					break;
				}
			}
		}
	}

	public void setErrorOnRelatedFields() {
		boolean noneOfKeyFieldsWereSetError = true;
		//
		/////////////////////////////////////////////////
		// Set error on the visible editable key field //
		/////////////////////////////////////////////////
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (dialog_.getDetailColumnList().get(j).isVisibleOnPanel()
						&& dialog_.getDetailColumnList().get(j).isEditable()
						&& dialog_.getDetailColumnList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
						&& !dialog_.getDetailColumnList().get(j).isError()) {
					dialog_.getDetailColumnList().get(j).setError(tableElement.getAttribute("Name") + res.getString("FunctionError45"));
					noneOfKeyFieldsWereSetError = false;
					break;
				}
			}
		}
		//
		if (noneOfKeyFieldsWereSetError) {
			//
			///////////////////////////////////////////////////////
			// Set error on the visible editable attribute field //
			///////////////////////////////////////////////////////
			for (int i = 0; i < fieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
					if (dialog_.getDetailColumnList().get(j).isVisibleOnPanel()
							&& !dialog_.getDetailColumnList().get(j).isNonEditableField()
							&& dialog_.getDetailColumnList().get(j).getFieldID().equals(fieldIDList.get(i))
							&& dialog_.getDetailColumnList().get(j).getTableAlias().equals(this.tableAlias)
							&& !dialog_.getDetailColumnList().get(j).isError()) {
						dialog_.getDetailColumnList().get(j).setError(tableElement.getAttribute("Name") + res.getString("FunctionError45"));
						noneOfKeyFieldsWereSetError = false;
						break;
					}
				}
			}
		}
		//
		if (noneOfKeyFieldsWereSetError) {
			//
			//////////////////////////////////////
			// Set error on the first key field //
			//////////////////////////////////////
			for (int i = 0; i < toKeyFieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
					if (dialog_.getDetailColumnList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
							&& !dialog_.getDetailColumnList().get(j).isError()) {
						dialog_.getDetailColumnList().get(j).setError(tableElement.getAttribute("Name") + res.getString("FunctionError45"));
						break;
					}
				}
			}
		}
	}
}

class XF310_HeaderComboBox extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private String dataTypeOptions_ = "";
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private int rows_ = 1;
	private String listType = "";
	private ArrayList<String> kubunKeyValueList = new ArrayList<String>();
	private ArrayList<XFHashMap> tableKeyValuesList = new ArrayList<XFHashMap>();
	private JTextField jTextField = new JTextField();
	private JPanel jPanelDummy = new JPanel();
	private JComboBox jComboBox = new JComboBox();
	private boolean isEditable = true;
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private XF310_HeaderReferTable referTable_ = null;
	private XF310 dialog_;
	private String oldValue = "";
	
	public XF310_HeaderComboBox(String dataSourceName, String dataTypeOptions, XF310 dialog, XF310_HeaderReferTable chainTable, boolean isNullable){
		//
		super();
		//
		StringTokenizer workTokenizer;
		org.w3c.dom.Element workElement;
		int fieldWidth = 0;
		String wrk = "";
		String strWrk;
		//
		dataTypeOptions_ = dataTypeOptions;
		workTokenizer = new StringTokenizer(dataSourceName, "." );
		tableAlias = workTokenizer.nextToken();
		tableID = tableAlias;
		referTable_ = chainTable;
		if (referTable_ != null && referTable_.getTableAlias().equals(tableAlias)) {
			tableID = referTable_.getTableID();
		}
		fieldID =workTokenizer.nextToken();
		dialog_ = dialog;
		//
		jTextField.setFont(new java.awt.Font("Dialog", 0, 14));
		jTextField.setEditable(false);
		jTextField.setFocusable(false);
		FontMetrics metrics = jTextField.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		jComboBox.setFont(new java.awt.Font("Dialog", 0, 14));
		jComboBox.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jComboBox.showPopup();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !jComboBox.isPopupVisible()) {
					requestFocus();
					dispatchEvent(e);
				}
			}
		});
		jComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (referTable_ != null && isEditable && jComboBox.getSelectedIndex() > -1) {
					referTable_.setKeyFieldValues(tableKeyValuesList.get(jComboBox.getSelectedIndex()));
				}
			}
		});
		jComboBox.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				setupRecordList();
			}
		});
		jPanelDummy.setPreferredSize(new Dimension(17, XFUtility.FIELD_UNIT_HEIGHT));
		//
		strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "VALUES");
		if (!strWrk.equals("")) {
			//
			listType = "VALUES_LIST";
			//
			if (isNullable) {
				jComboBox.addItem("");
			}
			//
			workTokenizer = new StringTokenizer(strWrk, ";" );
			while (workTokenizer.hasMoreTokens()) {
				wrk = workTokenizer.nextToken();
				jComboBox.addItem(wrk);
				if (metrics.stringWidth(wrk) > fieldWidth) {
					fieldWidth = metrics.stringWidth(wrk);
				}
			}
		} else {
			strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "KUBUN");
			if (!strWrk.equals("")) {
				//
				listType = "KUBUN_LIST";
				//
				if (isNullable) {
					kubunKeyValueList.add("");
					jComboBox.addItem("");
				}
				//
				try {
					Statement statement = dialog_.getSession().getConnection().createStatement();
					ResultSet result = statement.executeQuery("select KBUSERKUBUN,TXUSERKUBUN  from " + dialog_.getSession().getTableNameOfUserVariants() + " where IDUSERKUBUN = '" + strWrk + "' order by SQLIST");
					while (result.next()) {
						//
						kubunKeyValueList.add(result.getString("KBUSERKUBUN").trim());
						wrk = result.getString("TXUSERKUBUN").trim();
						jComboBox.addItem(wrk);
						if (metrics.stringWidth(wrk) > fieldWidth) {
							fieldWidth = metrics.stringWidth(wrk);
						}
					}
					result.close();
					//
					if (jComboBox.getItemCount() == 0) {
						JOptionPane.showMessageDialog(null, res.getString("FunctionError24") + dataSourceName + res.getString("FunctionError25"));
					}
				} catch (SQLException e) {
					e.printStackTrace(dialog_.getExceptionStream());
					dialog_.setErrorAndCloseFunction();
				}
			} else {
				//
				if (referTable_ != null) {
					//
					listType = "RECORDS_LIST";
					//
					keyFieldList = referTable_.getKeyFieldIDList();
					//
					workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
					ArrayList<String> workDataTypeOptionList = XFUtility.getOptionList(workElement.getAttribute("TypeOptions"));
					int dataSize = Integer.parseInt(workElement.getAttribute("Size"));
					if (workDataTypeOptionList.contains("KANJI")) {
						fieldWidth = dataSize * 14 + 10;
					} else {
						fieldWidth = dataSize * 7 + 10;
					}
					if (fieldWidth > 800) {
						fieldWidth = 800;
					}
				}
			}
		}
		//
		this.setSize(new Dimension(fieldWidth + 30, XFUtility.FIELD_UNIT_HEIGHT));
		this.setLayout(new BorderLayout());
		this.add(jComboBox, BorderLayout.CENTER);
		//this.setFocusable(false);
		this.addFocusListener(new ComponentFocusListener());
	}

	public void setupRecordList() {
		if (referTable_ != null && listType.equals("RECORDS_LIST")) {
			//
			tableKeyValuesList.clear();
			jComboBox.removeAllItems();
			//
			boolean blankItemRequired = true;
			XFHashMap keyValues = new XFHashMap();
			for (int i = 0; i < referTable_.getWithKeyFieldIDList().size(); i++) {
				for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
					if (referTable_.getWithKeyFieldIDList().get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
						if (dialog_.getHeaderFieldList().get(j).isNullable()) {
							//keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getHeaderFieldList().get(j).getNullValue());
							if (dialog_.getHeaderFieldList().get(j).isVisibleOnPanel()) {
								keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getHeaderFieldList().get(j).getValue());
							} else {
								keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getHeaderFieldList().get(j).getNullValue());
							}
						} else {
							blankItemRequired = false;
							break;
						}
					}
				}
				if (!blankItemRequired) {
					break;
				}
			}
			if (blankItemRequired) {
				tableKeyValuesList.add(keyValues);
				jComboBox.addItem("");
			}
			//
			try {
				String wrk = "";
				String sql = referTable_.getSelectSQL(true);
				Statement statement = dialog_.getSession().getConnection().createStatement();
				ResultSet result = statement.executeQuery(sql);
				while (result.next()) {
					if (referTable_.isRecordToBeSelected(result)) {
						//
						keyValues = new XFHashMap();
						for (int i = 0; i < keyFieldList.size(); i++) {
							keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), result.getString(keyFieldList.get(i)));
						}
						tableKeyValuesList.add(keyValues);
						//
						wrk = result.getString(fieldID).trim();
						jComboBox.addItem(wrk);
					}
				}
				result.close();
			} catch (SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
	}
	
	public void setEditable(boolean editable) {
		this.removeAll();
		if (editable) {
			this.add(jComboBox, BorderLayout.CENTER);
		} else {
			this.add(jPanelDummy, BorderLayout.EAST);
			this.add(jTextField, BorderLayout.CENTER);
		}
		isEditable = editable;
	}

	public Object getInternalValue() {
		String value = "";
		//
		if (jComboBox.getSelectedIndex() >= 0) {
			if (listType.equals("VALUES_LIST")) {
				value = jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
			}
			if (listType.equals("KUBUN_LIST")) {
				value = kubunKeyValueList.get(jComboBox.getSelectedIndex());
			}
			if (listType.equals("RECORDS_LIST")) {
				value = jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
			}
		}
		//
		return value;
	}
	
	public void setOldValue(Object obj) {
		oldValue = (String)obj;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getExternalValue() {
		return jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
	}
	
	public boolean isEditable() {
		return isEditable;
	}
	
	public void setValue(Object obj) {
		String value = (String)obj;
		value = value.trim();
		//
		//if (!this.isEditable) {
			if (listType.equals("VALUES_LIST")) {
				for (int i = 0; i < jComboBox.getItemCount(); i++) {
					if (jComboBox.getItemAt(i).toString().equals(value)) {
						jComboBox.setSelectedIndex(i);
						break;
					}
				}
			}
			if (listType.equals("KUBUN_LIST")) {
				for (int i = 0; i < kubunKeyValueList.size(); i++) {
					if (kubunKeyValueList.get(i).equals(value)) {
						jComboBox.setSelectedIndex(i);
						break;
					}
				}
			}
			if (listType.equals("RECORDS_LIST")) {
				//
				setupRecordList();
				if (jComboBox.getItemCount() > 0) {
					if (value == null || value.equals("")) {
						//jComboBox.setSelectedIndex(0);
					} else {
						for (int i = 0; i < jComboBox.getItemCount(); i++) {
							if (jComboBox.getItemAt(i).toString().equals(value)) {
								if (i != jComboBox.getSelectedIndex()) {
									jComboBox.setSelectedIndex(i);
								}
								break;
							}
						}
					}
				}
			}
			//
			if (jComboBox.getSelectedIndex() >= 0) {
				jTextField.setText(jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString());
			}
		//}
	}

	public void setBackground(Color color) {
		if (jComboBox != null) {
			jComboBox.setBackground(color);
		}
	}

	public int getRows() {
		return rows_;
	}

	class ComponentFocusListener implements FocusListener{
		public void focusLost(FocusEvent event){
		}
		public void focusGained(FocusEvent event){
			jComboBox.requestFocus();
		}
	}
}

class XF310_HeaderPromptCall extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private int rows_ = 1;
	private XFTextField xFTextField;
	private JButton jButton = new JButton();
	private boolean isEditable = true;
    private XF310 dialog_;
    private org.w3c.dom.Element functionElement_;
    private org.w3c.dom.Element fieldElement_;
    private ArrayList<XF310_HeaderReferTable> referTableList_;
    private String oldValue = "";
    //private Color normalColor;
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();

	public XF310_HeaderPromptCall(org.w3c.dom.Element fieldElement, org.w3c.dom.Element functionElement, XF310 dialog){
		//
		super();
		//
		fieldElement_ = fieldElement;
		functionElement_ = functionElement;
		dialog_ = dialog;
		//
		String fieldOptions = fieldElement_.getAttribute("FieldOptions");
		StringTokenizer workTokenizer = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		fieldID =workTokenizer.nextToken();
		//
		tableID = tableAlias;
		referTableList_ = dialog_.getHeaderReferTableList();
		for (int i = 0; i < referTableList_.size(); i++) {
			if (referTableList_.get(i).getTableAlias().equals(tableAlias)) {
				tableID = referTableList_.get(i).getTableID();
				break;
			}
		}
		//
		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID + "." + fieldID + res.getString("FunctionError11"));
		}
		String dataType = workElement.getAttribute("Type");
		String dataTypeOptions = workElement.getAttribute("TypeOptions");
		int dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (dataSize > 50) {
			dataSize = 50;
		}
		int decimalSize = 0;
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		//
		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_PUT");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToPutList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_PUT_TO");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToPutToList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_GET");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToGetList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_GET_TO");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToGetToList_.add(workTokenizer.nextToken());
			}
		}
		//
		xFTextField = new XFTextField(XFUtility.getBasicTypeOf(dataType), dataSize, decimalSize, dataTypeOptions, fieldOptions);
		xFTextField.setLocation(5, 0);
		//normalColor = xFTextField.getBackground();
		//
		jButton.setText("...");
		jButton.setFont(new java.awt.Font("Dialog", 0, 11));
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object value;
				//
				HashMap<String, Object> fieldValuesMap = new HashMap<String, Object>();
				for (int i = 0; i < fieldsToPutList_.size(); i++) {
					value = dialog_.getValueOfHeaderFieldByName(fieldsToPutList_.get(i));
					if (value != null) {
						fieldValuesMap.put(fieldsToPutToList_.get(i), value);
					}
				}
				//
				HashMap<String, Object> returnMap = dialog_.getSession().getFunction().execute(functionElement_, fieldValuesMap);
				if (!returnMap.get("RETURN_CODE").equals("99")) {
					HashMap<String, Object> fieldsToGetMap = new HashMap<String, Object>();
					for (int i = 0; i < fieldsToGetList_.size(); i++) {
						value = returnMap.get(fieldsToGetList_.get(i));
						if (value != null) {
							fieldsToGetMap.put(fieldsToGetToList_.get(i), value);
						}
					}
					for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
						value = fieldsToGetMap.get(dialog_.getHeaderFieldList().get(i).getDataSourceName());
						if (value != null) {
							dialog_.getHeaderFieldList().get(i).setValue(value);
						}
					}
				}
			}
		});
		jButton.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jButton.doClick();
					}
				}
			}
		});
		//
		this.setSize(new Dimension(xFTextField.getWidth() + 27, XFUtility.FIELD_UNIT_HEIGHT));
		this.setLayout(new BorderLayout());
		this.add(xFTextField, BorderLayout.CENTER);
		this.add(jButton, BorderLayout.EAST);
		this.setFocusable(true);
		this.addFocusListener(new ComponentFocusListener());
	}
	
	public boolean hasEditControlledKey() {
		boolean anyOfKeysAreEditControlled = false;
		//
		for (int i = 0; i < fieldsToGetToList_.size(); i++) {
			for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
				if (fieldsToGetToList_.get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
					if (!dialog_.getHeaderFieldList().get(j).isEditable() && dialog_.getHeaderTable().getTableID().equals(dialog_.getHeaderFieldList().get(j).getTableAlias())) {
						anyOfKeysAreEditControlled = true;
						break;
					}
				}
			}
		}
		//
		return anyOfKeysAreEditControlled;
	}

	public void setEditable(boolean editable) {
		isEditable = editable;
		xFTextField.setEditable(isEditable);
		xFTextField.setFocusable(isEditable);
	}

//	public void requestFocus() {
//		if (xFTextField.isEditable()) {
//			xFTextField.requestFocus();
//		} else {
//			jButton.requestFocus();
//		}
//	}

	public Object getInternalValue() {
		return xFTextField.getText();
	}

	public Object getExternalValue() {
		return xFTextField.getText();
	}
	
	public void setOldValue(Object obj) {
		oldValue = (String)obj;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public String getValueClass() {
		return "String";
	}
	
	public void setValue(Object obj) {
		if (obj == null) {
			xFTextField.setText("");
		} else {
			xFTextField.setText(obj.toString());
		}
	}

	public void setBackground(Color color) {
		if (xFTextField != null) {
			if (color.equals(XFUtility.ACTIVE_COLOR)) {
				//xFTextField.setBackground(normalColor);
				if (xFTextField.isEditable()) {
					xFTextField.setBackground(XFUtility.ACTIVE_COLOR);
				} else {
					xFTextField.setBackground(XFUtility.INACTIVE_COLOR);
				}
			}
			if (color.equals(XFUtility.ERROR_COLOR)) {
				xFTextField.setBackground(XFUtility.ERROR_COLOR);
			}
		}
	}

	public int getRows() {
		return rows_;
	}

	public boolean isEditable() {
		return isEditable;
	}

	class ComponentFocusListener implements FocusListener{
		public void focusLost(FocusEvent event){
		}
		public void focusGained(FocusEvent event){
			//jButton.requestFocus();
			if (xFTextField.isEditable()) {
				xFTextField.requestFocus();
			} else {
				jButton.requestFocus();
			}
		}
	}
}

class XF310_KeyInputDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private JPanel jPanelMain = new JPanel();
	private Dimension scrSize;
	private JPanel jPanelKeyFields = new JPanel();
	private JPanel jPanelTop = new JPanel();
	private JPanel jPanelBottom = new JPanel();
	private JPanel jPanelButtons = new JPanel();
	private JPanel jPanelInfo = new JPanel();
	private GridLayout gridLayoutInfo = new GridLayout();
	private JLabel jLabelFunctionID = new JLabel();
	private JLabel jLabelSessionID = new JLabel();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private JTextArea jTextAreaMessages = new JTextArea();
	private JButton jButtonCancel = new JButton();
	private JButton jButtonOK = new JButton();
	private XF310 dialog_;
	private final int FIELD_VERTICAL_MARGIN = 5;
	private final int FONT_SIZE = 14;
	private ArrayList<XF310_HeaderField> fieldList = new ArrayList<XF310_HeaderField>();
	private HashMap<String, Object> keyMap_ = new HashMap<String, Object>();
	
	public XF310_KeyInputDialog(XF310 dialog) {
		super(dialog, "", true);
		dialog_ = dialog;
		initComponentsAndVariants();
	}

	void initComponentsAndVariants() {
		//
		scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		//
		jPanelMain.setLayout(new BorderLayout());
		jPanelTop.setLayout(new BorderLayout());
		jPanelKeyFields.setLayout(null);
		jPanelKeyFields.setFocusable(false);
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jScrollPaneMessages.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setLayout(new BorderLayout());
		jPanelBottom.setBorder(null);
		jLabelFunctionID.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelFunctionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelFunctionID.setForeground(Color.gray);
		jLabelFunctionID.setFocusable(false);
		jLabelSessionID.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelSessionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelSessionID.setForeground(Color.gray);
		jLabelSessionID.setFocusable(false);
		jPanelButtons.setBorder(null);
		jPanelButtons.setLayout(null);
		jPanelButtons.setFocusable(false);
		gridLayoutInfo.setColumns(1);
		gridLayoutInfo.setRows(2);
		gridLayoutInfo.setVgap(4);
		jPanelInfo.setLayout(gridLayoutInfo);
		jPanelInfo.add(jLabelSessionID);
		jPanelInfo.add(jLabelFunctionID);
		jPanelInfo.setFocusable(false);
		jPanelMain.add(jPanelTop, BorderLayout.CENTER);
		jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		jPanelTop.add(jPanelKeyFields, BorderLayout.CENTER);
		jPanelTop.add(jScrollPaneMessages, BorderLayout.SOUTH);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		jButtonCancel.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jButtonCancel.setText(res.getString("Cancel"));
		jButtonCancel.setBounds(new Rectangle(5, 2, 80, 32));
		jButtonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				keyMap_.clear();
				setVisible(false);
			}
		});
		jButtonOK.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jButtonOK.setText("OK");
		jButtonOK.setBounds(new Rectangle(270, 2, 80, 32));
		jButtonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean anyOfFieldsAreNull = false;
				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).getInternalValue().equals(fieldList.get(i).getNullValue())) {
						anyOfFieldsAreNull = true;
						break;
					} else {
						keyMap_.put(fieldList.get(i).getFieldID(), fieldList.get(i).getInternalValue());
					}
				}
				if (anyOfFieldsAreNull) {
					jTextAreaMessages.setText(res.getString("FunctionError23"));
				} else {
					setVisible(false);
				}
			}
		});
		jPanelButtons.add(jButtonCancel);
		jPanelButtons.add(jButtonOK);
		this.getRootPane().setDefaultButton(jButtonOK);
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
		this.setSize(new Dimension(scrSize.width, scrSize.height));
		//
		StringTokenizer workTokenizer;
		String tableAlias, tableID, fieldID;
		org.w3c.dom.Element element;
		int posX = 0;
		int posY = 0;
		int biggestWidth = 0;
		int biggestHeight = 0;
		Dimension dim = new Dimension();
		Dimension dimOfPriviousField = new Dimension();
		boolean topField = true;
		XF310_HeaderField field;
		//
		this.setTitle(dialog_.getTitle());
		jLabelSessionID.setText(dialog_.getSession().getSessionID());
		jLabelFunctionID.setText(dialog_.getFunctionInfo());
		FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));
		//
		NodeList headerFieldElementList = dialog_.getFunctionElement().getElementsByTagName("Field");
		SortableDomElementListModel sortingList1 = XFUtility.getSortedListModel(headerFieldElementList, "Order");
		for (int i = 0; i < sortingList1.getSize(); i++) {
			element = (org.w3c.dom.Element)sortingList1.getElementAt(i);
			workTokenizer = new StringTokenizer(element.getAttribute("DataSource"), "." );
			tableAlias = workTokenizer.nextToken();
			tableID = dialog_.getTableIDOfTableAlias(tableAlias);
			fieldID =workTokenizer.nextToken();
			//
			if (tableID.equals(dialog_.getHeaderTable().getTableID())) {
				ArrayList<String> keyFieldList = dialog_.getHeaderTable().getKeyFieldIDList();
				for (int j = 0; j < keyFieldList.size(); j++) {
					if (keyFieldList.get(j).equals(fieldID)) {
						field = new XF310_HeaderField((org.w3c.dom.Element)sortingList1.getElementAt(i), dialog_);
						field.setEditable(true);
						fieldList.add(field);
						//
						if (topField) {
							posX = 0;
							posY = this.FIELD_VERTICAL_MARGIN + 8;
							topField = false;
						} else {
							posX = 0;
							posY = posY + dimOfPriviousField.height+ field.getPositionMargin() + this.FIELD_VERTICAL_MARGIN;
						}
						dim = field.getPreferredSize();
						field.setBounds(posX, posY, dim.width, dim.height);
						jPanelKeyFields.add(field);
						//
						if (posX + dim.width > biggestWidth) {
							biggestWidth = posX + dim.width;
						}
						if (posY + dim.height > biggestHeight) {
							biggestHeight = posY + dim.height;
						}
						dimOfPriviousField = new Dimension(dim.width, dim.height);
					}
				}
			}
		}
		//
		int width = 450;
		if (biggestWidth > 430) {
			width = biggestWidth + 20;
		}
		int height = biggestHeight + 117;
		this.setPreferredSize(new Dimension(width, height));
		posX = (scrSize.width - width) / 2;
		posY = (scrSize.height - height) / 2;
		this.setLocation(posX, posY);
		this.pack();
	}
	
	public HashMap<String, Object> requestKeyValues(String message) {
		keyMap_.clear();
		if (message.equals("")) {
			jTextAreaMessages.setText(res.getString("FunctionMessage29"));
		} else {
			jTextAreaMessages.setText(message);
		}
		this.setVisible(true);
		//
		return keyMap_;
	}
}

class XF310_jTableMain_focusAdapter extends java.awt.event.FocusAdapter {
	XF310 adaptee;
	XF310_jTableMain_focusAdapter(XF310 adaptee) {
		this.adaptee = adaptee;
	}
	public void focusGained(FocusEvent e) {
		adaptee.jTableMain_focusGained(e);
	}
	public void focusLost(FocusEvent e) {
		adaptee.jTableMain_focusLost(e);
	}
}

class XF310_jScrollPaneTable_mouseAdapter extends java.awt.event.MouseAdapter {
	XF310 adaptee;
	XF310_jScrollPaneTable_mouseAdapter(XF310 adaptee) {
		this.adaptee = adaptee;
	}
	public void mousePressed(MouseEvent e) {
		adaptee.jScrollPaneTable_mousePressed(e);
	}
}

class XF310_FunctionButton_actionAdapter implements java.awt.event.ActionListener {
	XF310 adaptee;
	XF310_FunctionButton_actionAdapter(XF310 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jFunctionButton_actionPerformed(e);
	}
}
