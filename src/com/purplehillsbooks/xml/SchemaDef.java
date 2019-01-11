/*
 * Copyright 2013 Keith D Swenson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.purplehillsbooks.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Deprecated
public class SchemaDef extends Mel {
	public SchemaDef(Document doc, Element ele) {
		super(doc, ele);
	}

	public boolean isContainer() {
		return ("container".equals(getName()));
	}

	@Deprecated
	public Mel declareAttribute(String attrName) throws Exception {
		Mel child = findChild("attr", "name", attrName, Mel.class);
		if (child == null) {
			// if it gets here, then it did not exist before
			// so we need to add it
			child = addChild("attr", Mel.class);
			child.setAttribute("name", attrName);
		}
		return child;
	}

	@Deprecated
	public Mel declareChild(String childName) throws Exception {
		Mel child = findChild("contains", "name", childName, Mel.class);
		if (child == null) {
			// if it gets here, then it did not exist before
			// so we need to add it
			child = addChild("contains", Mel.class);
			child.setAttribute("name", childName);
		}
		return child;
	}

	@Deprecated
	public void declareChild(String childName, boolean allowPlural) throws Exception {
		Mel child = declareChild(childName);
		child.setAttribute("plural", allowPlural ? "true" : "false");
	}

	@Deprecated
	public boolean childIsPlural(String childName) throws Exception {
		Mel child = findChild("contains", "name", childName, Mel.class);
		if (child == null) {
			return false;
		}
		return ("true".equals(child.getAttribute("plural")));
	}

}
