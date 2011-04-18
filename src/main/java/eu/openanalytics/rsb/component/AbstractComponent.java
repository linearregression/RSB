/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2011
 *
 *   ===========================================================================
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.openanalytics.rsb.component;

import java.io.File;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.openanalytics.rsb.config.Configuration;

/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public abstract class AbstractComponent {
    private final Log logger = LogFactory.getLog(getClass());

    @Resource
    private Configuration configuration;

    // exposed for unit testing
    void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    protected Log getLogger() {
        return logger;
    }

    protected File getApplicationResultDirectory(final String applicationName) {
        return new File(getConfiguration().getRsbResultsDirectory(), applicationName);
    }
}