/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.io.xml.layout;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.List;
import org.apereo.portal.io.xml.IPortalData;
import org.apereo.portal.io.xml.IPortalDataType;
import org.apereo.portal.io.xml.SimpleStringPortalData;
import org.apereo.portal.layout.dlm.ConfigurationLoader;
import org.apereo.portal.layout.dlm.FragmentDefinition;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Lists each fragment owner in the portal
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
public class FragmentLayoutsDataFunction
    implements Function<IPortalDataType, Iterable<? extends IPortalData>> {
  private ConfigurationLoader configurationLoader;

  @Autowired
  public void setConfigurationLoader(ConfigurationLoader configurationLoader) {
    this.configurationLoader = configurationLoader;
  }

  @Override
  public Iterable<? extends IPortalData> apply(IPortalDataType input) {
    final List<FragmentDefinition> fragments = this.configurationLoader.getFragments();

    final List<IPortalData> portalData =
        Lists.transform(
            fragments,
            new Function<FragmentDefinition, IPortalData>() {
              @Override
              public IPortalData apply(FragmentDefinition fragmentDefinition) {
                return new SimpleStringPortalData(
                    fragmentDefinition.getOwnerId(),
                    fragmentDefinition.getName(),
                    fragmentDefinition.getDescription());
              }
            });

    return portalData;
  }
}
