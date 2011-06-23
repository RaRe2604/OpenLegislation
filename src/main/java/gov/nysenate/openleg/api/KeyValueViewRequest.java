package gov.nysenate.openleg.api;

import gov.nysenate.openleg.api.servlets.ApiServlet.ApiEnum;
import gov.nysenate.openleg.model.SenateObject;
import gov.nysenate.openleg.model.bill.Bill;
import gov.nysenate.openleg.search.SearchEngine;
import gov.nysenate.openleg.search.SenateResponse;
import gov.nysenate.openleg.util.TextFormatter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KeyValueViewRequest extends AbstractApiRequest {
	String format;
	String key;
	String value;
	
	public KeyValueViewRequest(HttpServletRequest request, HttpServletResponse response,
			String format, String key, String value, String pageNumber, String pageSize) {
		super(request, response, pageNumber, pageSize, getApiEnum(KeyValueView.values(),key));
		this.format = thisOrThat(format, DEFAULT_FORMAT);
		this.key = key;
		this.value = value;
	}

	@Override
	public void fillRequest() {
		String urlPath = TextFormatter.append("/legislation/", key, "/", value, "/");

		String sFormat = "json";
		String sortField = "sortindex";
		boolean sortOrder = false;
		
		QueryBuilder queryBuilder = new QueryBuilder();
		
		String filter = request.getParameter("filter");
		
		SenateResponse sr = null;

		// now calculate start, end idx based on pageIdx and pageSize
		int start = (pageNumber - 1) * pageSize;

		try {
			
			queryBuilder.keyValue(key, value).and().current().and().active();
			if(filter != null) queryBuilder.and().insertAfter(filter);
			
			sr = SearchEngine.getInstance().search(queryBuilder.query(), sFormat,
					start, pageSize, sortField, sortOrder);

			sr.setResults(ApiHelper.buildSearchResultList(sr));
			
			request.setAttribute("sortField", sortField);
			request.setAttribute("sortOrder", Boolean.toString(sortOrder));
			request.setAttribute("type", key);
			request.setAttribute("term", queryBuilder.query());
			request.setAttribute("format", format);
			request.setAttribute(PAGE_IDX, pageNumber + "");
			request.setAttribute(PAGE_SIZE, pageSize + "");
			request.setAttribute("urlPath", urlPath);
			request.setAttribute("results", sr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getView() {
		return TextFormatter.append("/views/search-", format, ".jsp");
	}
	
	@Override
	public boolean isValid() {
		return key != null && value != null;
	}
	
	public enum KeyValueView implements ApiEnum {
		SPONSOR("sponsor", 		Bill.class, 	new String[] {"html", "json", "xml", "rss"}),
		COMMITTEE("committee", 	Bill.class, 	new String[] {"html", "json", "xml", "rss"});
		
		public final String view;
		public final Class<? extends SenateObject> clazz;
		public final String[] formats;
		
		private KeyValueView(final String view, final Class<? extends SenateObject> clazz, final String[] formats) {
			this.view = view;
			this.clazz = clazz;
			this.formats = formats;
		}
		
		public String view() {
			return view;
		}
		public String[] formats() {
			return formats;
		}
		public Class<? extends SenateObject> clazz() {
			return clazz;
		}
	}
}
