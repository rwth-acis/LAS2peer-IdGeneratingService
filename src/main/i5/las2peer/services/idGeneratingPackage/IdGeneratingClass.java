package i5.las2peer.services.idGeneratingPackage;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.restMapper.annotations.GET;
import i5.las2peer.restMapper.annotations.POST;
import i5.las2peer.restMapper.annotations.Path;
import i5.las2peer.restMapper.annotations.PathParam;
import i5.las2peer.restMapper.annotations.Produces;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.annotations.swagger.ApiInfo;
import i5.las2peer.restMapper.annotations.swagger.ApiResponse;
import i5.las2peer.restMapper.annotations.swagger.ApiResponses;
import i5.las2peer.restMapper.annotations.swagger.Notes;
import i5.las2peer.restMapper.annotations.swagger.ResourceListApi;
import i5.las2peer.restMapper.annotations.swagger.Summary;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.Context;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.idGeneratingPackage.database.DatabaseManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;

/**
 * LAS2peer Id Generating Service
 * 
 * This is a LAS2peer service that returns Unique ids when it is called.
 * 
 */
@Path("generateId")
@Version("0.1.2")
@ApiInfo(title = "Id Generation", description = "<p>A RESTful service for creating unique ids.</p>", termsOfServiceUrl = "", contact = "bakiu@dbis.rwth-aachen.de", license = "MIT", licenseUrl = "https://github.com/rwth-acis/LAS2peer-IdGeneratingService/blob/master/LICENSE")
public class IdGeneratingClass extends Service {

	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private String epUrl;
	private DatabaseManager dbm;

	public IdGeneratingClass() {

		setFieldValues();
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass,
				jdbcUrl, jdbcSchema);
	}

	/**
	 * Method to generate new Id, store it in the database, and return the newly
	 * inserted value.
	 * 
	 * @return HttpResponse
	 */

	@POST
	@Path("id")
	@ResourceListApi(description = "Generated identifier.")
	@Summary("Generate and store a new Id. The generated value is returned.")
	@Notes("Requires authentication.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Id returned and saved successfully."),
			@ApiResponse(code = 401, message = "User is not authenticated."),
			@ApiResponse(code = 500, message = "Internal error.") })
	public HttpResponse addNewId(@ContentParam String data) {

		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		String existingId = "";

		try {
			JSONObject o;
			try {
				o = (JSONObject) JSONValue.parseWithException(data);
			} catch (ParseException e1) {
				throw new IllegalArgumentException("Data is not valid JSON!");
			}
			// if(getActiveAgent().getId() !=
			// getActiveNode().getAnonymous().getId()){
			Object callingServiceObj = new String("service");
			// Object callingMethodObj = new String("calling_method");
			// Object oidcUserObj = new String("OIDC_user");
			Object hashValueObj = new String("hashValue");

			String callingService = "";
			// String callingMethod = "";
			// String oidcUser = "";
			String hashValue = "";

			callingService = getKeyFromJSON(callingServiceObj, o, false);
			// callingMethod = getKeyFromJSON(callingMethodObj, o, false);
			// oidcUser = getKeyFromJSON(oidcUserObj, o, false);
			hashValue = getKeyFromJSON(hashValueObj, o, false);

			conn = dbm.getConnection();

			if (!callingService.equals("")) {

				String selectQuery = "SELECT id FROM id_generated WHERE "
						+ " hashValue = '" + hashValue + "'"
						+ " and service = '" + callingService + "'";

				// create the java statement
				Statement selectSt = conn.createStatement();

				// execute the query, and get a java resultset
				ResultSet rsSelect = selectSt.executeQuery(selectQuery);

				// iterate through the java resultset
				if (rsSelect.next()) {
					existingId = rsSelect.getString("id");
					selectSt.close();
					// return
					HttpResponse r = new HttpResponse(existingId);
					r.setStatus(200);
					return r;
				} else {

					PreparedStatement preparedStatement = null;
					preparedStatement = conn.prepareStatement(
							"INSERT INTO id_generated(service,hashValue)"
									+ "					 VALUES (?,?);",
							Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setString(1, callingService);
					// preparedStatement.setString(4, new
					// Timestamp(date.getTime()).toString());
					preparedStatement.setString(2, hashValue);

					preparedStatement.executeUpdate();
					if (preparedStatement.getUpdateCount() >= 0) {
						ResultSet keys = preparedStatement.getGeneratedKeys();
						keys.next();
						int key = keys.getInt(1);
						String id = key + "";

						// return
						HttpResponse r = new HttpResponse(id);
						r.setStatus(200);
						return r;
					} else {
						String result = "Could not get an Id.";
						// return
						HttpResponse r = new HttpResponse(result);
						r.setStatus(500);
						return r;
					}
				}

			} else {
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: "
						+ "Missing JSON object member with key \""
						+ callingServiceObj.toString() + "\" ");
				er.setStatus(400);
				return er;
			}

			/*
			 * }else{ result = "User in not authenticated";
			 * 
			 * // return HttpResponse r = new HttpResponse(result);
			 * r.setStatus(401); return r; }
			 */

		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: "
					+ e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources if exception or not
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());

					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: "
							+ e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());

					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: "
							+ e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());

					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: "
							+ e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
		}
	}

	/**
	 * Read the value stored for the given key in the json input. The value is
	 * stored at value and returns false if the given Json object does not
	 * contain the give key
	 * 
	 * @param key
	 *            vale of which is requested
	 * @param json
	 *            input json
	 * @param remove
	 *            if set removes the key from the json object.
	 * @return value output value
	 */
	private String getKeyFromJSON(Object key, JSONObject json, boolean remove) {
		String value = "";
		if (json.containsKey(key)) {
			value = (String) json.get(key);
			if (remove) {
				json.remove(key);
			}

		}
		return value;
	}

	// ================= Swagger Resource Listing & API Declarations
	// =====================

	@GET
	@Path("api-docs")
	@Summary("retrieve Swagger 1.2 resource listing.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Swagger 1.2 compliant resource listing"),
			@ApiResponse(code = 404, message = "Swagger resource listing not available due to missing annotations."), })
	@Produces(MediaType.APPLICATION_JSON)
	public HttpResponse getSwaggerResourceListing() {
		return RESTMapper.getSwaggerResourceListing(this.getClass());
	}

	@GET
	@Path("api-docs/{tlr}")
	@Produces(MediaType.APPLICATION_JSON)
	@Summary("retrieve Swagger 1.2 API declaration for given top-level resource.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Swagger 1.2 compliant API declaration"),
			@ApiResponse(code = 404, message = "Swagger API declaration not available due to missing annotations."), })
	public HttpResponse getSwaggerApiDeclaration(@PathParam("tlr") String tlr) {
		return RESTMapper.getSwaggerApiDeclaration(this.getClass(), tlr, epUrl);
	}

	/**
	 * Method for debugging purposes. Here the concept of restMapping validation
	 * is shown. It is important to check, if all annotations are correct and
	 * consistent. Otherwise the service will not be accessible by the
	 * WebConnector. Best to do it in the unit tests. To avoid being
	 * overlooked/ignored the method is implemented here and not in the test
	 * section.
	 * 
	 * @return true, if mapping correct
	 */
	public boolean debugMapping() {
		String XML_LOCATION = "./restMapping.xml";
		String xml = getRESTMapping();

		try {
			RESTMapper.writeFile(XML_LOCATION, xml);
		} catch (IOException e) {
			e.printStackTrace();
		}

		XMLCheck validator = new XMLCheck();
		ValidationResult result = validator.validate(xml);

		if (result.isValid())
			return true;
		return false;
	}

	/**
	 * This method is needed for every RESTful application in LAS2peer. There is
	 * no need to change!
	 * 
	 * @return the mapping
	 */
	public String getRESTMapping() {
		String result = "";
		try {
			result = RESTMapper.getMethodsAsXML(this.getClass());
		} catch (Exception e) {

			e.printStackTrace();
		}
		return result;
	}

}
