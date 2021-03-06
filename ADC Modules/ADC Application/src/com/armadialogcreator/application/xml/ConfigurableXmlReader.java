package com.armadialogcreator.application.xml;

import com.armadialogcreator.application.ADCData;
import com.armadialogcreator.application.ADCDataListManager;
import com.armadialogcreator.util.UTF8FileReader;
import com.armadialogcreator.util.XmlParseException;
import com.armadialogcreator.util.XmlReader;
import com.armadialogcreator.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileNotFoundException;

/**
 @author K
 @since 01/07/2019 */
public class ConfigurableXmlReader<D extends ADCData> {
	private final File f;
	private final ADCDataListManager<D> manager;

	public ConfigurableXmlReader(@NotNull File f, @NotNull ADCDataListManager<D> manager) {
		this.f = f;
		this.manager = manager;
	}

	public void read() throws XmlParseException {
		XmlReader reader;
		try {
			reader = new XmlReader(new UTF8FileReader(f));
		} catch (FileNotFoundException e) {
			throw new XmlParseException(XmlParseException.Reason.FileNotFound, e);
		}
		for (Element element : XmlUtil.iterateChildElements(reader.getDocumentElement())) {
			for (D d : manager.getDataList()) {
				if (d.getDataID().equals(element.getTagName())) {
					d.loadFromConfigurable(new XMLNodeConfigurable(element));
				}
			}
		}
	}
}
