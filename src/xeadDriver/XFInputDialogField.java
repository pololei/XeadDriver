package xeadDriver;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

////////////////////////////////////////////////////////////////
//This is a public class used in Table-Script.               //
//Note that public classes are defined in its own java file. //
////////////////////////////////////////////////////////////////
public class XFInputDialogField extends JPanel {
	private static final long serialVersionUID = 1L;
	private int size_ = 10;
	private int decimal_ = 0;
	private String parmID_ = "";
	private String inputType_ = "";
	private JLabel jLabelField = new JLabel();
	private JButton jButton = null;
	private Component component = null;
	private FontMetrics metrics;
	private ArrayList<Object> valueList = new ArrayList<Object>();
	private boolean isEditable_ = true;
	private boolean isAutoSizing = true;
	private XFInputDialog dialog_ = null;
	private String functionID_ = "";
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();

    public XFInputDialogField(String fieldCaption, String inputType, String parmID, XFInputDialog dialog) {
		super();
		parmID_ = parmID;
    	if (!inputType.equals("ALPHA")
    			&& !inputType.equals("KANJI")
    			&& !inputType.equals("NUMERIC")
    			&& !inputType.equals("DATE")
    			&& !inputType.equals("LISTBOX")
    			&& !inputType.equals("CHECKBOX")) {
    		inputType_ = "ALPHA";
    	}
		inputType_ = inputType;
		dialog_ = dialog;
		jLabelField.setText(fieldCaption + " ");
		jLabelField.setFocusable(false);
		jLabelField.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelField.setVerticalAlignment(SwingConstants.TOP);
		jLabelField.setFont(new java.awt.Font("Dialog", 0, 14));
		metrics = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		jLabelField.setPreferredSize(new Dimension(120, XFUtility.FIELD_UNIT_HEIGHT));
		if (metrics.stringWidth(fieldCaption) > 120) {
			jLabelField.setFont(new java.awt.Font("Dialog", 0, 12));
			metrics = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 12));
			if (metrics.stringWidth(fieldCaption) > 120) {
				jLabelField.setFont(new java.awt.Font("Dialog", 0, 10));
			}
		}
		if (inputType_.equals("ALPHA")
				|| inputType_.equals("KANJI")
				|| inputType_.equals("NUMERIC")) {
			JTextField field = new JTextField();
			field.addFocusListener(new ComponentFocusListener());
			if (inputType_.equals("NUMERIC")) {
				field.setHorizontalAlignment(SwingConstants.RIGHT);
				field.setDocument(new LimitedDocument(this));
			}
			component = field;
		}
		if (inputType_.equals("DATE")) {
			XFDateField field = new XFDateField(dialog_.getSession());
			component = field;
		}
		if (inputType_.equals("LISTBOX")) {
			JComboBox field = new JComboBox();
			component = field;
		}
		if (inputType_.equals("CHECKBOX")) {
			JCheckBox field = new JCheckBox();
			component = field;
		}
		component.setFont(new java.awt.Font("Monospaced", 0, 14));
		metrics = component.getFontMetrics(new java.awt.Font("Monospaced", 0, 14));
		this.setOpaque(false);
		if (inputType_.equals("DATE")) {
			int fieldWidth = XFUtility.getWidthOfDateValue(dialog_.getSession().getDateFormat(), 14);
			this.setBounds(this.getBounds().x, this.getBounds().y, 150 + fieldWidth, XFUtility.FIELD_UNIT_HEIGHT);
		} else {
			this.setBounds(this.getBounds().x, this.getBounds().y, 150, XFUtility.FIELD_UNIT_HEIGHT);
		}
		this.setLayout(new BorderLayout());
		this.add(jLabelField, BorderLayout.WEST);
		this.add(component, BorderLayout.CENTER);
   }
    
   public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		if (inputType_.equals("ALPHA")
				|| inputType_.equals("KANJI")
				|| inputType_.equals("NUMERIC")) {
			((JTextField)component).setEditable(isEditable_);
			((JTextField)component).setFocusable(isEditable_);
		}
		if (inputType_.equals("DATE")) {
			((XFDateField)component).setEditable(isEditable_);
			((XFDateField)component).setFocusable(isEditable_);
			int fieldWidth = XFUtility.getWidthOfDateValue(dialog_.getSession().getDateFormat(), 14);
			if (isEditable_) {
				this.setBounds(this.getBounds().x, this.getBounds().y, 150 + fieldWidth, XFUtility.FIELD_UNIT_HEIGHT);
			} else {
				this.setBounds(this.getBounds().x, this.getBounds().y, 124 + fieldWidth, XFUtility.FIELD_UNIT_HEIGHT);
			}
		}
		if (inputType_.equals("LISTBOX")) {
			((JComboBox)component).setEditable(isEditable_);
			((JComboBox)component).setFocusable(isEditable_);
		}
		if (inputType_.equals("CHECKBOX")) {
			((JCheckBox)component).setEnabled(isEditable_);
			((JCheckBox)component).setFocusable(isEditable_);
		}
   }
   
   public boolean isEditable() {
	   return isEditable_;
   }
    
   public void setSize(int size) {
	   size_ = size;
	   setFieldWidth();
   }
   
   public void setDecimal(int decimal) {
	   decimal_ = decimal;
	   setFieldWidth();
   }

   private void setFieldWidth() {
	   int width = 0;
	   int charWidth = 7;
	   int length = size_;
	   //
	   if (inputType_.equals("KANJI")) {
		   charWidth = 14;
	   }
	   if (inputType_.equals("NUMERIC")) {
		   for (int i = size_-1 ; i > 0; i--) {
			   if (i%3 == 0) {
				   length++;
			   }
		   }
	   }
	   if (decimal_ > 0) {
		   width = charWidth * (length + decimal_ + 2 + 1);
	   } else {
		   width = charWidth * (length + 1);
	   }
	   if (width < 50) {
		   width = 50;
	   }
	   if (jButton == null) {
		   this.setBounds(this.getBounds().x, this.getBounds().y, width + 125, this.getBounds().height);
	   } else {
		   this.setBounds(this.getBounds().x, this.getBounds().y, width + 125 + 26, this.getBounds().height);
	   }
	   isAutoSizing = false;
   }
   
   public void setValue(Object value) {
	   if (value == null) {
		   value = "";
	   }
	   if (inputType_.equals("ALPHA")
			   || inputType_.equals("KANJI")
			   || inputType_.equals("NUMERIC")) {
		   if (inputType_.equals("ALPHA") || inputType_.equals("KANJI")) {
			   //((JTextField)component).setText(value.toString());
			   String strValue = value.toString();
			   if (strValue.length() > size_) {
				   strValue = strValue.substring(0, size_);
			   }
			   ((JTextField)component).setText(strValue);
		   }
		   if (inputType_.equals("NUMERIC")) {
			   String stringValue = "";
			   if (decimal_ == 0) {
				   stringValue = XFUtility.getFormattedIntegerValue(getStringNumber(value.toString()), new ArrayList<String>(), size_);
			   } else {
				   stringValue = XFUtility.getFormattedFloatValue(getStringNumber(value.toString()), decimal_);
			   }
			   ((JTextField)component).setText(stringValue);
		   }
		   if (isAutoSizing) {
			   int width = this.getBounds().width - 130;
			   if (metrics.stringWidth(value.toString()) > width) {
				   this.setBounds(this.getBounds().x, this.getBounds().y, metrics.stringWidth(value.toString()) + 130, this.getBounds().height);
			   }
		   }
	   }
	   if (inputType_.equals("DATE")) {
		   ((XFDateField)component).setValue(value.toString());
	   }
	   if (inputType_.equals("LISTBOX")) {
			for (int i = 0; i < valueList.size(); i++) {
				if (value == valueList.get(i) || value.toString().equals(valueList.get(i).toString())) {
					((JComboBox)component).setSelectedIndex(i);
					break;
				}
			}
	   }
	   if (inputType_.equals("CHECKBOX")) {
		   if (value == Boolean.TRUE || value.toString().equals("true")) {
			   ((JCheckBox)component).setSelected(true);
		   } else {
			   ((JCheckBox)component).setSelected(false);
		   }
	   }
   }

   public String getStringNumber(String text) {
	   String numberString = XFUtility.getStringNumber(text);
	   if (numberString.equals("")) {
		   if (decimal_ == 0) {
			   numberString = "0";
		   }
		   if (decimal_ == 1) {
			   numberString = "0.0";
		   }
		   if (decimal_ == 2) {
			   numberString = "0.00";
		   }
		   if (decimal_ == 3) {
			   numberString = "0.000";
		   }
		   if (decimal_ == 4) {
			   numberString = "0.0000";
		   }
		   if (decimal_ == 5) {
			   numberString = "0.00000";
		   }
		   if (decimal_ == 6) {
			   numberString = "0.000000";
		   }
	   }
	   return numberString;
   }
   
   public Object getValue() {
		if (inputType_.equals("ALPHA") || inputType_.equals("KANJI")) {
			return ((JTextField)component).getText();
		}
		if (inputType_.equals("NUMERIC")) {
			if (decimal_ == 0) {
				return Integer.parseInt(getStringNumber(((JTextField)component).getText()));
			} else {
				return Float.parseFloat(getStringNumber(((JTextField)component).getText()));
			}
		}
		if (inputType_.equals("DATE")) {
			return ((XFDateField)component).getInternalValue();
		}
		if (inputType_.equals("LISTBOX")) {
			return valueList.get(((JComboBox)component).getSelectedIndex());
		}
		if (inputType_.equals("CHECKBOX")) {
			return ((JCheckBox)component).isSelected();
		}
		return null;
   }
   
   public void addItem(String text, Object value) {
		if (inputType_.equals("LISTBOX")) {
			((JComboBox)component).addItem(text);
			int width = this.getBounds().width - 150;
			if (metrics.stringWidth(text) > width) {
				this.setBounds(this.getBounds().x, this.getBounds().y, metrics.stringWidth(text) + 150, this.getBounds().height);
			}
			valueList.add(value);
		}
   }
   
   public Object getItem() {
		if (inputType_.equals("LISTBOX")) {
			return ((JComboBox)component).getItemAt(((JComboBox)component).getSelectedIndex());
		} else {
			return null;
		}
   }
   
   public void setPrompter(String functionID, String sendFrom, String sendTo, String receiveFrom, String receiveTo) {
		if (inputType_.equals("ALPHA") || inputType_.equals("NUMERIC")) {
			//
			functionID_ = functionID;
			//
			StringTokenizer workTokenizer;
			if (!sendFrom.equals("")) {
				workTokenizer = new StringTokenizer(sendFrom, ";" );
				while (workTokenizer.hasMoreTokens()) {
					fieldsToPutList_.add(workTokenizer.nextToken());
				}
			}
			if (!sendTo.equals("")) {
				workTokenizer = new StringTokenizer(sendTo, ";" );
				while (workTokenizer.hasMoreTokens()) {
					fieldsToPutToList_.add(workTokenizer.nextToken());
				}
			}
			if (!receiveFrom.equals("")) {
				workTokenizer = new StringTokenizer(receiveFrom, ";" );
				while (workTokenizer.hasMoreTokens()) {
					fieldsToGetList_.add(workTokenizer.nextToken());
				}
			}
			if (!receiveTo.equals("")) {
				workTokenizer = new StringTokenizer(receiveTo, ";" );
				while (workTokenizer.hasMoreTokens()) {
					fieldsToGetToList_.add(workTokenizer.nextToken());
				}
			}
			//
			jButton = new JButton();
			ImageIcon imageIcon = new ImageIcon(xeadDriver.XFInputDialogField.class.getResource("prompt.png"));
		 	jButton.setIcon(imageIcon);
			jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
			this.add(jButton, BorderLayout.EAST);
			this.setBounds(this.getBounds().x, this.getBounds().y, this.getBounds().width + 26, this.getBounds().height);
			jButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Object value;
					try {
						setCursor(new Cursor(Cursor.WAIT_CURSOR));
						//
						HashMap<String, Object> fieldValuesMap = new HashMap<String, Object>();
						for (int i = 0; i < fieldsToPutList_.size(); i++) {
							value = dialog_.getValueOfFieldByParmID(fieldsToPutList_.get(i));
							if (value != null) {
								fieldValuesMap.put(fieldsToPutToList_.get(i), value);
							}
						}
						//
						HashMap<String, Object> returnMap = dialog_.getSession().executeFunction(functionID_, fieldValuesMap);
						if (!returnMap.get("RETURN_CODE").equals("99")) {
							HashMap<String, Object> fieldsToGetMap = new HashMap<String, Object>();
							for (int i = 0; i < fieldsToGetList_.size(); i++) {
								value = returnMap.get(fieldsToGetList_.get(i));
								if (value != null) {
									fieldsToGetMap.put(fieldsToGetToList_.get(i), value);
								}
							}
							for (int i = 0; i < dialog_.getFieldList().size(); i++) {
								value = fieldsToGetMap.get(dialog_.getFieldList().get(i).getParmID());
								if (value != null) {
									dialog_.getFieldList().get(i).setValue(value);
								}
							}
						}
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					} finally {
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				}
			});
		}
   }

   public String getParmID() {
	   return parmID_;
   }

   class ComponentFocusListener implements FocusListener{
	   public void focusLost(FocusEvent event){
		   getInputContext().setCompositionEnabled(false);
	   }
	   public void focusGained(FocusEvent event){
		   Character.Subset[] subsets  = new Character.Subset[] {java.awt.im.InputSubset.LATIN_DIGITS};
		   String lang = Locale.getDefault().getLanguage();
		   if (inputType_.equals("KANJI")) {
			   if (lang.equals("ja")) {
				   subsets = new Character.Subset[] {java.awt.im.InputSubset.KANJI};
			   }
			   if (lang.equals("ko")) {
				   subsets = new Character.Subset[] {java.awt.im.InputSubset.HANJA};
			   }
			   if (lang.equals("zh")) {
				   subsets = new Character.Subset[] {java.awt.im.InputSubset.TRADITIONAL_HANZI};
			   }
			   getInputContext().setCharacterSubsets(subsets);
			   getInputContext().setCompositionEnabled(true);
		   } else {
			   getInputContext().setCharacterSubsets(subsets);
			   getInputContext().setCompositionEnabled(false);
		   }
	   }
   }
   
   class LimitedDocument extends PlainDocument {
	   private static final long serialVersionUID = 1L;
	   XFInputDialogField adaptee;
	   LimitedDocument(XFInputDialogField adaptee) {
		   this.adaptee = adaptee;
	   }
	   public void insertString(int offset, String str, AttributeSet attr) {
		   try {
			   int integerSizeOfField = adaptee.size_ - adaptee.decimal_;
			   if (adaptee.decimal_ > 0 && str.length() == 1) {
				   String wrkStr0 = super.getText(0, super.getLength());
				   wrkStr0 = wrkStr0.substring(0, offset) + str + wrkStr0.substring(offset, wrkStr0.length());
				   String wrkStr1 = wrkStr0.replace(".", "");
				   wrkStr1 = wrkStr1.replace(",", "");
				   wrkStr1 = wrkStr1.replace("-", "");
				   if (wrkStr1.length() > adaptee.size_) {
					   wrkStr1 = wrkStr1.substring(0, integerSizeOfField) + "." + wrkStr1.substring(integerSizeOfField, wrkStr1.length() - 1);
					   super.replace(0, super.getLength(), wrkStr1, attr);
				   } else {
					   int posOfDecimal = wrkStr0.indexOf(".");
					   if (posOfDecimal == -1) {
						   if (wrkStr1.length() > integerSizeOfField) {
							   wrkStr1 = wrkStr1.substring(0, integerSizeOfField) + "." + wrkStr1.substring(integerSizeOfField, wrkStr1.length());
							   super.replace(0, super.getLength(), wrkStr1, attr);
						   } else {
							   super.insertString( offset, str, attr );
						   }
					   } else {
						   int decimalLengthOfInputData = wrkStr0.length() - posOfDecimal - 1;
						   if (decimalLengthOfInputData <= adaptee.decimal_) {
							   super.insertString( offset, str, attr );
						   }
					   }
				   }
			   } else {
				   if (str.contains(".")) {
					   JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("NumberFormatError"));
				   } else {
					   String wrkStr0 = super.getText(0, super.getLength());
					   wrkStr0 = wrkStr0.substring(0, offset) + str + wrkStr0.substring(offset, wrkStr0.length());
					   String wrkStr1 = wrkStr0.replace(".", "");
					   wrkStr1 = wrkStr1.replace(",", "");
					   wrkStr1 = wrkStr1.replace("-", "");
					   if (wrkStr1.length() <= adaptee.size_) {
						   super.insertString( offset, str, attr );
					   }
				   }
			   }
		   } catch (BadLocationException e) {
			   e.printStackTrace();
		   }
	   }
   }
}
