//test for 666666666666
//test for 1021
//test for inc
package com.smee.form.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.smee.form.common.impl.IFormPanelPreProcess;
import com.smee.utils.ZJUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * @author zhuming
 *
 * @description 
 * ��ȡpreference�����Ϣ����form panel��preference�ĸ�ʽ���£�
 * <Form Type>_PropertyGroupShownPref��String Array��������ʾform����Ҫ��ʾ����tab
 * <Form Type>_PropertyGroupShownPref_<Tab Name>��String Array��������ʾform��tab����Ҫ��ʾ��Щ����
 * 
 * ���Ը�ʽ��
 * <Property Name> �Զ���ı�����
 * Form.<Property Name> ����ϵͳ���ԣ�����object_name, object_type, owning_user
 * Item.<Property Name> �����Ա��Ӧ��Item��ϵͳ����
 * ItemRevision.<Property Name> �����Ա��Ӧ��ItemRevision��ϵͳ����
 * 
 * �ӿ�˵����
 * public void formComponentPreCheck(FormComponent formComponent)
 * �ڱ�����formComponent����property�ؼ�֮ǰ�Ĵ��������޸�formComponent�Ĳ����Դ�����ͬ��property�ؼ�
 * 
 * public void formComponentPostCheck(FormComponent formComponent)
 * �ڱ�����formComponent����property�ؼ�֮��Ĵ��������޸��Ѿ������õ�property�ؼ�����������¼��ȵ�
 * 
 * public void formComponentsProcess()
 * �����еı�����formComponent���������֮��Ĵ���������������ӿؼ�֮��Ļ����߼�
 * 
 * public void addExtraComponents()
 * �����������������Զ����formComponent��������������form�޹ص����Բ���ʾ��ָ����tabҳ�ϣ�������Ҫ����
 * AbstractFormRendering��preSaveProcess������ʵ�ֶ���Щ���Եı������
 * 
 * public void preDisplayProcess()
 * ����ʾ֮ǰ�Ĵ������ʱ������ԣ�ϵͳ���ԺͶ������Ե�formComponent���Ѿ�������ϲ��Ѿ����浽��Ӧ��FormTab�ϣ�
 * ���Զ�FormTab���д������޸����յ���ʾ
 * 
 * 
 * @UpdateHistory
 * 2010-09-27 ����iTabbedPane���޸���ʾģʽΪPageBar
 * 
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractFormPanel extends JPanel implements IFormPanelPreProcess {
	
	public TCProperty[] properties = null;
	
	public Hashtable<String, FormComponent> componentsTable;
	public Vector<String> componentNames;
	
	public Hashtable<String, FormComponent> systemComponentTable;
	public Vector<FormComponent> systemComponentsList;
	
	public Hashtable<String, FormComponent> extraComponentTable;
	public Vector<FormComponent> extraComponentList;
	public Vector<String> extraComponentNames;
	
	public Hashtable<String, FormTab> formTabsTable;
	public Vector<FormTab> formTabs;
	
	public TCComponentForm form;
	
	public FormPanelDescriptor descriptor;
	
	public String[] tabNames;
	
	public TCPreferenceService prefService;
	
	public boolean isTabbed = false;
	
	public TCSession session;
	
	public TCComponentItem item;
	public TCComponentItemRevision revision;
	
	public TCProperty[] visibleProperties = null;
	
	
	/**
	 * ������TC����
	 * @param tcComponentForm form����
	 * @param formPanelDescriptor  Panel�Ĺ�������
	 * @param tabbed  �Ƿ�tabble
	 */
	public AbstractFormPanel(TCComponentForm tcComponentForm, FormPanelDescriptor formPanelDescriptor, boolean tabbed,String formLookType) {
		this(tcComponentForm, null, formPanelDescriptor, tabbed,formLookType);
	}
	
	
	/**
	 * ����TC����
	 * @param tcComponentForm
	 * @param properties ��Ҫ��ʾ������
	 * @param formPanelDescriptor
	 * @param tabbed
	 */
	public AbstractFormPanel(TCComponentForm tcComponentForm, TCProperty[] properties, FormPanelDescriptor formPanelDescriptor, boolean tabbed,String formLookType) {
		super(new BorderLayout());//JPanel
		setBackground(Color.white);//������ɫ
		
		form = tcComponentForm;
		visibleProperties = properties;
		descriptor = formPanelDescriptor;
		isTabbed = tabbed;
		
		if (form == null || descriptor == null) {//form����Ϊ�գ�descriptor��Ҫʵ����
			return;
		}
		
		try {
			TCComponent tmp_comp = form.getTCProperty("item_revision").getReferenceValue();
			
			if (tmp_comp != null) {//�Ƿ�Ϊ�汾�µ�form
				revision = (TCComponentItemRevision) tmp_comp;
				item = revision.getItem();
			}
		} 
		catch (TCException e2) {
			e2.printStackTrace();
		}
		
		try {
			session = (TCSession) form.getSession();
			prefService = session.getPreferenceService();
			String prefName = form.getType() + "_PropertyGroupShownPref";
			tabNames = prefService.getStringArray(0, prefName);
			
			if (tabNames.length == 0) {
				isTabbed = false;
			}
		} 
		catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		
		try {	
			if("".equals(formLookType)){
				properties=form.getFormTCProperties();
			}else{
				String formtypename=form.getTypeComponent().getName();
				String[] rules = ZJUtils.getPreferenceValueArray(formtypename+formLookType);//����ѡ�������ļ�
				if(rules==null||rules.length==0){
					MessageBox.post("δ�ҵ���ѡ���ֵ������ϵ����Ա��", "������ʾ", MessageBox.ERROR);
					return;
				}
				properties=form.getTCProperties(rules);
				for(int j=0;j<properties.length;j++){
					if(properties[j]==null){
						MessageBox.post("��ѡ�����õ�"+rules[j]+"��ֵ�Ҳ�����Ӧ�����ԣ�����ϵ����Ա��", "������ʾ", MessageBox.ERROR);
						return;
					}
				}
			}
		}
		catch (TCException e) {
			System.out.println("Failed to get form tc properties, " + e.getMessage());
			return;
		}
		

		
		componentsTable = new Hashtable<String, FormComponent>();
		componentNames = new Vector<String>();
		
		extraComponentTable = new Hashtable<String, FormComponent>();
		extraComponentList = new Vector<FormComponent>();
		extraComponentNames = new Vector<String>();
		
		systemComponentTable = new Hashtable<String, FormComponent>();
		systemComponentsList = new Vector<FormComponent>();
		
		formTabs = new Vector<FormTab>();
		formTabsTable = new Hashtable<String, FormTab>();

		for (int i = 0; i < properties.length; i++) {
			//������TC�Դ���form����ȥ����ֻ��Ҫ�Զ��������
			if(isTCFormProperty(properties[i].getPropertyName())){
				continue;
			}
			//��װFormComponent����
			FormComponent formComponent = new FormComponent(properties[i], descriptor, form, false);
			
			
			//�ڱ�����formComponent����property�ؼ�֮ǰ�Ĵ��������޸�formComponent�Ĳ����Դ�����ͬ��property�ؼ�
			formComponentPreCheck(formComponent);
			formComponent.createPropertyComponent();
			//�ڱ�����formComponent����property�ؼ�֮��Ĵ��������޸��Ѿ������õ�property�ؼ�����������¼��ȵ�
			formComponentPostCheck(formComponent);
						
			componentsTable.put(properties[i].getPropertyName(), formComponent);
			componentNames.add(properties[i].getPropertyName());
		}
		
		//�����������������Զ����formComponent��������������form�޹ص����Բ���ʾ��ָ����tabҳ�ϣ�������Ҫ����AbstractFormRendering��preSaveProcess������ʵ�ֶ���Щ���Եı������
		addExtraComponents();
		
		if (isTabbed) {//����Table
			
			for (int i = 0; i < tabNames.length; i++) {
				try {
					String prefName = form.getType() + "_PropertyGroupShownPref_" + tabNames[i];
					String[] propNames = prefService.getStringArray(0, prefName);
					
					FormTab formTab = new FormTab(tabNames[i]);
					
					for (int j = 0; j < propNames.length; j++) {
						
						FormComponent formComponent = null;
						
						if (propNames[j].startsWith("Form.") || propNames[j].startsWith("Item.") || propNames[j].startsWith("ItemRevision.")) {
							String propName = "";
							TCProperty property = null;
							TCComponent systemComponent = null;
							
							if (propNames[j].startsWith("Form.")) {
								propName = propNames[j].substring("Form.".length());
								property = form.getTCProperty(propName);
								systemComponent = form;
							}
							else if (propNames[j].startsWith("Item.")) {
								if (item == null) {
									continue;
								}
								propName = propNames[j].substring("Item.".length());
								property = item.getTCProperty(propName);
								systemComponent = item;
							}
							else {
								if (revision == null) {
									continue;
								}
								propName = propNames[j].substring("ItemRevision.".length());
								property = revision.getTCProperty(propName);
								systemComponent = revision;
							}
							
							formComponent = new FormComponent(property,descriptor, form, false);
							formComponent.setPropName(propNames[j]);
							formComponent.setTabName(tabNames[i]);
							formComponent.setSystemComponent(systemComponent);
							//�ڱ�����formComponent����property�ؼ�֮ǰ�Ĵ��������޸�formComponent�Ĳ����Դ�����ͬ��property�ؼ�
							formComponentPreCheck(formComponent);
							formComponent.createPropertyComponent();
							//�ڱ�����formComponent����property�ؼ�֮��Ĵ��������޸��Ѿ������õ�property�ؼ�����������¼��ȵ�
							formComponentPostCheck(formComponent);

							systemComponentTable.put(propNames[j], formComponent);
							systemComponentsList.add(formComponent);
							
							formTab.addTabComponent(propNames[j], formComponent);
						}
						
						formComponent = (FormComponent) extraComponentTable.get(propNames[j]);
						
						if (formComponent != null) {
							formComponent.setTabName(tabNames[i]);
							formTab.addTabComponent(propNames[j], formComponent);
							
							for (int k = 0; k < extraComponentList.size(); k++) {
								FormComponent component = extraComponentList.elementAt(k);
								if (component.getPropName().equals(propNames[j])) {
									extraComponentList.removeElementAt(k);
									break;
								}
							}
							
							continue;
						}
						
						formComponent = (FormComponent) componentsTable.get(propNames[j]);
						
						if (formComponent != null) {
							formComponent.setTabName(tabNames[i]);
							formTab.addTabComponent(propNames[j], formComponent);
							continue;
						}
					}
					
					for (int j = 0; j < extraComponentList.size(); j++) {
						FormComponent component = extraComponentList.elementAt(j);
						if (component.getTabName().equals(tabNames[i])) {
							formTab.addTabComponent(component.getPropName(), component);
						}
					}
					
					if (formTab.tabPropNames.size() > 0) {
						formTabs.add(formTab);
						formTabsTable.put(tabNames[i], formTab);
					}
				}
				catch (TCException e) {
					e.printStackTrace();
				}
			}
			
			
		}else{//û��Table
			
			FormTab formTab = new FormTab("General");
			
			for (int i = 0; i < properties.length; i++) {
				if(isTCFormProperty(properties[i].getPropertyName())){
					continue;
				}
				FormComponent formComponent = (FormComponent) componentsTable.get(properties[i].getPropertyName());
				formTab.addTabComponent(properties[i].getPropertyName(), formComponent);
			}
			
			if (formTab.tabPropNames.size() > 0) {
				formTabs.add(formTab);
				formTabsTable.put("General", formTab);
			}
		}
		
		//����visible������
		if (visibleProperties != null) {//��������properties��ֵ
			
			FormComponent[] formComponents = getAllFormComponents();
			
			for (int i = 0; i < formComponents.length; i++) {
				formComponents[i].setVisible(false);
			}
			
			for (int i = 0; i < visibleProperties.length; i++) {
				FormComponent formComponent = getFormComponent(visibleProperties[i].getPropertyName());
				
				if (formComponent != null) {
					formComponent.setVisible(true);
				}
			}
		}
		
		//�����еı�����formComponent���������֮��Ĵ���������������ӿؼ�֮��Ļ����߼�
		formComponentsProcess();
		
		//����ʾ֮ǰ�Ĵ������ʱ������ԣ�ϵͳ���ԺͶ������Ե�formComponent���Ѿ�������ϲ��Ѿ����浽��Ӧ��FormTab�ϣ����Զ�FormTab���д������޸����յ���ʾ
		preDisplayProcess();

		JPanel tabPanel = new JPanel(new VerticalLayout(0, 0, 0, 0, 0));
					
		for (int i = 0; i < formTabs.size(); i++) {//����ÿ��formTab����
			FormTab formTab = formTabs.elementAt(i);
			
			JPanel pagePanel = new JPanel(new BorderLayout());
			
			JPanel topPanel = new JPanel(new PropertyLayout(20, 10, 10, 10, 10, 10));
			JPanel bottomPanel = new JPanel(new PropertyLayout(20, 10, 10, 10, 10, 10));
			
			topPanel.setBackground(Color.white);
			bottomPanel.setBackground(Color.white);
			
			int topPanelRow = 0;//��
			int bottomPanelRow = 0;
			
			for (int j = 0; j < formTab.tabPropNames.size(); j++) {
				FormComponent formComponent = formTab.getTabComponent(formTab.tabPropNames.elementAt(j));
				
				if (formComponent.isVisible == true) {
					if (formComponent.getPropType() == FormComponent.PROP_table) {
						bottomPanelRow++;
						bottomPanel.add(String.valueOf(bottomPanelRow) + ".1.left.top.preferred.preferred", new JLabel(formComponent.getDisplayName()));
						bottomPanelRow++;
						bottomPanel.add(String.valueOf(bottomPanelRow) + ".1.left.top.preferred.preferred", (Component) formComponent.getPropertyComponent());
					
					}else {
						topPanelRow++;
						topPanel.add(String.valueOf(topPanelRow) + ".1.right.top.preferred.preferred", new JLabel(formComponent.getDisplayName()));
						topPanel.add(String.valueOf(topPanelRow) + ".2.left.top.preferred.preferred", (Component) formComponent.getPropertyComponent());

					}
				}
			}
			
			pagePanel.add(BorderLayout.NORTH, topPanel);
			pagePanel.add(BorderLayout.CENTER, bottomPanel);
			
			JScrollPane jScrollPane = new JScrollPane(pagePanel);
			tabPanel.add("unbound", jScrollPane);
			
			formTab.formPanel = jScrollPane;
		}
		
		add("Center", tabPanel);
	}
		
	
	public void addExtraComponent(String componentName, FormComponent formComponent) {
		extraComponentTable.put(componentName, formComponent);
		extraComponentList.add(formComponent);
		extraComponentNames.add(componentName);
	}
	
	
	public FormComponent getFormComponent(String propName) {
		FormComponent formComponent = null;
		
		formComponent = componentsTable.get(propName);
		
		if (formComponent == null) {
			formComponent = systemComponentTable.get(propName);
		}
		
		if (formComponent == null) {
			formComponent = extraComponentTable.get(propName);
		}
		
		return formComponent;
	}
	
	
	public FormComponent[] getAllFormComponents() {
		FormComponent[] formComponents = new FormComponent[componentNames.size() + systemComponentsList.size() + extraComponentNames.size()];
		
		int i = 0;
		
		for (i = 0; i < componentNames.size(); i++) {
			formComponents[i] = componentsTable.get(componentNames.elementAt(i));
		}
		
		for (int j = 0; j < systemComponentsList.size(); j++, i++) {
			formComponents[i] = systemComponentsList.elementAt(j);
		}
		
		for (int j = 0; j < extraComponentNames.size(); j++, i++) {
			formComponents[i] = extraComponentTable.get(extraComponentNames.elementAt(j));
		}
		
		return formComponents;
	}
	
	
	public FormTab getFormTabAt(int index) {
		if (formTabs.size() > index) {
			return formTabs.elementAt(index);
		}
		else {
			return null;
		}
	}
	
	
	public FormTab[] getAllFormTabs() {
		FormTab[] tabs = new FormTab[formTabs.size()];
		
		for (int i = 0; i < formTabs.size(); i++) {
			tabs[i] = formTabs.elementAt(i);
		}
		
		return tabs;
	}
	
	
	public FormTab getFormTab(String tabName) {
		return formTabsTable.get(tabName);
	}
	
	//�����������������Զ����formComponent��������������form�޹ص����Բ���ʾ��ָ����tabҳ�ϣ�������Ҫ����AbstractFormRendering��preSaveProcess������ʵ�ֶ���Щ���Եı������
	public abstract void addExtraComponents();
	
	
	//ȥ��TC�Դ�����
	public static boolean isTCFormProperty(String propertyName){
		List list=new ArrayList();
		list.add("user_data_1");
		list.add("user_data_2");
		list.add("user_data_3");
		list.add("serial_number");
		list.add("item_comment");
		list.add("previous_version_id");
		list.add("project_id");
		
		if(list.contains(propertyName)){
			return true;
		}
		
		return false;
	}
	
	
	

}
