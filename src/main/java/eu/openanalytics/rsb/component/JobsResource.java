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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractJob.Type;
import eu.openanalytics.rsb.rest.types.JobToken;
import eu.openanalytics.rsb.rest.types.ObjectFactory;

/**
 * Handles asynchronous R job processing requests.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@Component("jobsResource")
@Path("/jobs")
public class JobsResource extends AbstractComponent {
    // FIXME unit test
    // FIXME support application/zip application/x-zip application/x-zip-compressed
    // FIXME support multipart/form-data
    private final static ObjectFactory restOF = new ObjectFactory();

    private final static class BadRequestStatus implements StatusType {
        private final String reasonPhrase;

        private BadRequestStatus(final String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
        }

        public int getStatusCode() {
            return Status.BAD_REQUEST.getStatusCode();
        }

        public Family getFamily() {
            return Status.BAD_REQUEST.getFamily();
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }
    };

    @Autowired
    private JmsTemplate jmsTemplate;

    /**
     * Handles a function call job with a JSON payload.
     * 
     * @param jsonArgument
     *            Argument passed to the function called on RServi.
     * @param httpHeaders
     * @return
     * @throws URISyntaxException
     */
    @POST
    @Consumes({ Constants.JSON_JOB_CONTENT_TYPE })
    @Produces({ Constants.JSON_JOB_CONTENT_TYPE })
    public JobToken handleJsonFunctionCallJob(final String jsonArgument, @Context final HttpHeaders httpHeaders,
            @Context final UriInfo uriInfo) throws URISyntaxException {

        return handleFunctionCallJob(jsonArgument, httpHeaders, uriInfo, Type.JSON_FUNCTION_CALL);
    }

    /**
     * Handles a function call job with a XML payload.
     * 
     * @param xmlArgument
     *            Argument passed to the function called on RServi.
     * @param httpHeaders
     * @return
     * @throws URISyntaxException
     */
    @POST
    @Consumes({ Constants.XML_JOB_CONTENT_TYPE })
    @Produces({ Constants.XML_JOB_CONTENT_TYPE })
    public JobToken handleXmlFunctionCallJob(final String xmlArgument, @Context final HttpHeaders httpHeaders,
            @Context final UriInfo uriInfo) throws URISyntaxException {

        return handleFunctionCallJob(xmlArgument, httpHeaders, uriInfo, Type.XML_FUNCTION_CALL);
    }

    private JobToken handleFunctionCallJob(final String argument, final HttpHeaders httpHeaders, final UriInfo uriInfo, final Type jobType)
            throws URISyntaxException {

        final String applicationName = Util.getSingleHeader(httpHeaders, Constants.APPLICATION_NAME_HTTP_HEADER);
        if (!Util.isValidApplicationName(applicationName)) {
            throw new WebApplicationException(Response.status(
                    new BadRequestStatus("Bad request - Invalid application name: " + applicationName)).build());
        }

        final String jobId = UUID.randomUUID().toString();

        final AbstractJob job = jobType.build(applicationName, jobId, argument, getJobMeta(httpHeaders));

        jmsTemplate.send(Util.getJobsQueueName(applicationName), new MessageCreator() {
            public Message createMessage(final Session session) throws JMSException {
                return session.createObjectMessage(job);
            }
        });

        return buildResponse(uriInfo, httpHeaders, applicationName, jobId);
    }

    private Map<String, String> getJobMeta(final HttpHeaders httpHeaders) {
        final Map<String, String> meta = new HashMap<String, String>();

        for (final Entry<String, List<String>> multiValues : httpHeaders.getRequestHeaders().entrySet()) {
            if (!StringUtils.startsWithIgnoreCase(multiValues.getKey(), Constants.RSB_META_HEADER_PREFIX)) {
                continue;
            }

            if (multiValues.getValue().size() > 1) {
                throw new WebApplicationException(Response.status(
                        new BadRequestStatus("Bad request - Multiple values found for header: " + multiValues.getKey())).build());
            }

            meta.put(multiValues.getKey(), multiValues.getValue().get(0));
        }

        return meta;
    }

    private JobToken buildResponse(final UriInfo uriInfo, final HttpHeaders httpHeaders, final String applicationName, final String jobId)
            throws URISyntaxException {
        final JobToken jobToken = restOF.createJobToken();
        jobToken.setApplicationName(applicationName);
        jobToken.setJobId(jobId);

        final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path("results").path(applicationName);

        final String uriOverride = Util.getSingleHeader(httpHeaders, Constants.URI_OVERRIDE_HTTP_HEADER);
        if (StringUtils.isNotBlank(uriOverride)) {
            final URI override = new URI(uriOverride);
            uriBuilder.scheme(override.getScheme());
            uriBuilder.host(override.getHost());
            uriBuilder.port(override.getPort());
        }

        jobToken.setApplicationResultsUri(uriBuilder.build().toString());
        jobToken.setResultUri(uriBuilder.path(jobId).build().toString());
        return jobToken;
    }
}
