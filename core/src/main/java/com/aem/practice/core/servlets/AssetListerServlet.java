package com.aem.practice.core.servlets;

import java.util.List;

import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;

@Component(immediate = true, service = Servlet.class, property = { "sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=" + "/bin/santosh/assetlister", "sling.servlet.extensions=" + "json" })
public class AssetListerServlet extends SlingSafeMethodsServlet {

	// Generated serialVersionUID
	private static final long serialVersionUID = 7762806638577908286L;

	// Default logger
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	// Instance of ResourceResolver
	private ResourceResolver resourceResolver;

	// JCR Session instance
	private Session session;

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {

		try {

			// Getting the ResourceResolver from the current request
			resourceResolver = request.getResourceResolver();

			// Getting the session instance by adapting ResourceResolver
			session = resourceResolver.adaptTo(Session.class);

			QueryManager queryManager = session.getWorkspace().getQueryManager();
			String queryString = "SELECT asset FROM [dam:Asset] AS asset WHERE ISDESCENDANTNODE(asset ,'/content/dam/we-retail/en/activities/hiking')";
			Query query = queryManager.createQuery(queryString, "JCR-SQL2");

			QueryResult queryResult = query.execute();
			response.setContentType("application/json");
			// response.getWriter().println("--------------Result-------------");
			RowIterator rowIterator = queryResult.getRows();
			// Map<String, String> map = new HashMap<String, String>();
			JSONObject json = new JSONObject();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.nextRow();
				Resource res = resourceResolver.getResource(row.getPath());
				Asset asset = res.adaptTo(Asset.class);
				// response.getWriter().println(asset.getName());
				List<Rendition> renditions = asset.getRenditions();
				json.put(asset.getName(), renditions.get(1).getPath());
				// response.getWriter().println(renditions.get(0).getPath());
			}

			response.getWriter().println(json.toString());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (resourceResolver != null) {
				resourceResolver.close();
			}
		}
	}

}