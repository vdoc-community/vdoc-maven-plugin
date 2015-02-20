package com.vdoc.maven.plugin.generate.classes.authenticate.jaxb;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by famaridon on 20/05/2014.
 */
public class Body {

	protected Token token;

	@XmlElement
	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}
}
