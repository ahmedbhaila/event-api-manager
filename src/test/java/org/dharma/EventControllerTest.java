package org.dharma;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.dharma.controller.EventController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@WebMvcTest(EventController.class)
public class EventControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	JedisConnectionFactory jedisConnection;

	@Autowired
	RedisTemplate<String, String> redisTemplate;

	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

	@Before
	public void setUp() {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.context).apply(springSecurity())
				.apply(documentationConfiguration(this.restDocumentation)).build();
		// jedisConnection.getConnection().flushDb();
		// redisTemplate.opsForValue().set("admin", "admin");
		// jedisConnection.getConnection().close();
	}

	@Test
	public void postMandatoryParametersForCreatingEvent() throws Exception {

		String json = "{\"name\": \"testEvent3\", \"description\": \"test description\", \"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		MvcResult result = this.mvc
				.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
						.with(httpBasic("admin", "admin")))
				.andExpect(status().isCreated()).andDo(document("postEvent")).andReturn();

		String eventId = result.getResponse().getContentAsString();

		// use the eventId above to do a GET
		this.mvc.perform(get("/v1/event/" + eventId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.name").value("testEvent3"))
				.andExpect(jsonPath("$.description").value("test description"))
				.andExpect(jsonPath("$.location").value("chicago,il"))
				// .andExpect(jsonPath("$.dateTime").value("2017-09-26T20:11:43.00Z"))
				.andExpect(jsonPath("$.isPublic").value("false"));
	}

	@Test
	public void postAllParametersForCreatingEvent() throws Exception {

		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"123,123\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		MvcResult result = this.mvc
				.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
						.with(httpBasic("admin", "admin")))
				.andExpect(status().isCreated()).andDo(document("postAllEvent")).andReturn();

		String eventId = result.getResponse().getContentAsString();

		// use the eventId above to do a GET
		this.mvc.perform(get("/v1/event/" + eventId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.name").value("testEvent3"))
				.andExpect(jsonPath("$.description").value("test description"))
				.andExpect(jsonPath("$.location").value("chicago,il"))
				.andExpect(jsonPath("$.webUrl").value("http://localhost/img"))
				.andExpect(jsonPath("$.photo").value("img")).andExpect(jsonPath("$.geoLocation").value("123,123"))
				// .andExpect(jsonPath("$.dateTime").value("2017-09-26T20:11:43.00Z"))
				.andExpect(jsonPath("$.isPublic").value("false")).andDo(document("getEvent"));
	}

	@Test
	public void postMissingMandatoryParametersForCreatingEvent() throws Exception {

		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"123,123\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isBadRequest());

	}

	@Test
	public void postBadFormattedParametersForCreatingEvent() throws Exception {

		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"123,123\", "
				+ "\"webUrl\": \"htt://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isBadRequest());

	}

	@Test
	public void updateEvent() throws Exception {

		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"123,123\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		String eventId = result.getResponse().getContentAsString();

		// use the eventId above to do a GET
		result = this.mvc.perform(get("/v1/event/" + eventId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.name").value("testEvent3"))
				.andExpect(jsonPath("$.description").value("test description"))
				.andExpect(jsonPath("$.location").value("chicago,il"))
				.andExpect(jsonPath("$.webUrl").value("http://localhost/img"))
				.andExpect(jsonPath("$.photo").value("img")).andExpect(jsonPath("$.geoLocation").value("123,123"))
				// .andExpect(jsonPath("$.dateTime").value("2017-09-26T20:11:43.00Z"))
				.andExpect(jsonPath("$.isPublic").value("false")).andReturn();

		String updatedJson = result.getResponse().getContentAsString().replace("test description",
				"test updated description");

		this.mvc.perform(put("/v1/event/" + eventId.split(":")[1]).contentType(MediaType.APPLICATION_JSON)
				.content(updatedJson).with(httpBasic("admin", "admin"))).andExpect(status().isNoContent())
				.andDo(document("putEvent")).andReturn();

		// do a GET again
		this.mvc.perform(get("/v1/event/" + eventId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.name").value("testEvent3"))
				.andExpect(jsonPath("$.description").value("test updated description"))
				.andExpect(jsonPath("$.location").value("chicago,il"))
				.andExpect(jsonPath("$.webUrl").value("http://localhost/img"))
				.andExpect(jsonPath("$.photo").value("img")).andExpect(jsonPath("$.geoLocation").value("123,123"))
				// .andExpect(jsonPath("$.dateTime").value("2017-09-26T20:11:43.00Z"))
				.andExpect(jsonPath("$.isPublic").value("false")).andReturn();

	}

	@Test
	public void deleteEvent() throws Exception {

		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"123,123\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		String eventId = result.getResponse().getContentAsString();

		// get total events so far
		result = this.mvc.perform(get("/v1/events/total").with(httpBasic("admin", "admin"))).andExpect(status().isOk())
				.andDo(document("getTotalEvents")).andReturn();
		long a = Long.valueOf(result.getResponse().getContentAsString());

		// use the eventId above to do a GET
		this.mvc.perform(get("/v1/event/" + eventId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.name").value("testEvent3"))
				.andExpect(jsonPath("$.description").value("test description"))
				.andExpect(jsonPath("$.location").value("chicago,il"))
				.andExpect(jsonPath("$.webUrl").value("http://localhost/img"))
				.andExpect(jsonPath("$.photo").value("img")).andExpect(jsonPath("$.geoLocation").value("123,123"))
				// .andExpect(jsonPath("$.dateTime").value("2017-09-26T20:11:43.00Z"))
				.andExpect(jsonPath("$.isPublic").value("false")).andReturn();

		// delete this event
		this.mvc.perform(delete("/v1/event/" + eventId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isNoContent()).andDo(document("deleteEvent"));

		// check total events index updated

		// get total events so far
		result = this.mvc.perform(get("/v1/events/total").with(httpBasic("admin", "admin"))).andExpect(status().isOk())
				.andDo(document("getTotalEvents")).andReturn();
		long b = Long.valueOf(result.getResponse().getContentAsString());
		
		// assert one event less
		Assert.assertTrue(b == a - 1);

	}

	@Test
	public void testForNonExistantEvent() throws Exception {
		// use the eventId above to do a GET
		this.mvc.perform(get("/v1/event/-1").with(httpBasic("admin", "admin"))).andExpect(status().isNotFound());
	}

	@Test
	public void testForGetPaginatedEventsDefault() throws Exception {

		// add one record
		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"123,123\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		result.getResponse().getContentAsString();

		// default option shows all records
		// do a GET for events, count should be >= 1

		this.mvc.perform(get("/v1/events").with(httpBasic("admin", "admin"))).andExpect(status().isFound())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1)))).andDo(document("getPaginatedEventDefault"))
				.andReturn();

	}

	@Test
	public void testForGetPaginatedEventsSingle() throws Exception {

		// add one record
		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"123,123\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		result.getResponse().getContentAsString();

		// paginated to show
		// do a GET for events, count should be >= 1

		this.mvc.perform(get("/v1/events?startIndex=1&pageSize=1").with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$", hasSize(equalTo(1))))
				.andDo(document("getPaginatedEventSingle")).andReturn();

	}

	@Test
	public void testForGetPaginatedEventsMultiple() throws Exception {

		// add three records record
		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"123,123\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		for (int i = 0; i < 3; i++) {

			MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
					.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

			result.getResponse().getContentAsString();
		}

		// paginated to show
		// do a GET for events, count should be == 3

		this.mvc.perform(get("/v1/events?startIndex=1&pageSize=3").with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$", hasSize(equalTo(3))))
				.andDo(document("getPaginatedEventMultiple")).andReturn();

	}

	@Test
	public void testForGetPaginatedEventsWithBadPagination() throws Exception {

		// add three records record
		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"123,123\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		result.getResponse().getContentAsString();

		// paginated to show
		// do a GET for events, count should be 0

		this.mvc.perform(get("/v1/events?startIndex=-1&pageSize=3").with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$", hasSize(equalTo(0)))).andReturn();

	}

	// Single Filter related tests

	@Test
	public void testForGetPublicEvents() throws Exception {

		// add a public event
		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"123,123\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"true\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		result.getResponse().getContentAsString();

		// paginated to show
		// do a GET for events, count should be 0
		// // Search criteria format: ?searchCriteria=isPublic:true

		this.mvc.perform(get("/v1/events?searchCriteria=isPublic:true").with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
				// .andExpect(jsonPath("$..isPublic").value("true"))
				.andDo(document("getPublicEvents")).andReturn();

	}

	@Test
	public void testForGetSpecificLocationEvents() throws Exception {

		// add a location based event
		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"123,123\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Burbank,CA\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"true\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		result.getResponse().getContentAsString();

		// paginated to show
		// do a GET for events, count should be 0
		// // Search criteria format: ?searchCriteria=location:Burbank,CA

		this.mvc.perform(get("/v1/events?searchCriteria=location:burbank,ca").with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
				// .andExpect(jsonPath("$..location").value("Burbank,CA"))
				.andDo(document("getSpecificLocationEvents")).andReturn();

	}

	@Test
	public void testForGetSpecificGeoLocationEvents() throws Exception {

		// add a location based event
		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"22,21\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Burbank,CA\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"true\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		result.getResponse().getContentAsString();

		// paginated to show
		// do a GET for events, count should be 0
		// // Search criteria format: ?searchCriteria=geoLocation:22,21

		this.mvc.perform(get("/v1/events?searchCriteria=geoLocation:22,21").with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
				// .andExpect(jsonPath("$..geoLocation").value("22,21"))
				.andDo(document("GetSpecificGeoLocationEvents")).andReturn();

	}

	@Test
	public void testForGetSpecificNamedEvents() throws Exception {

		// add a location based event
		String json = "{\"name\": \"Special Event\", " + "\"geoLocation\": \"22,21\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Burbank,CA\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"true\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		result.getResponse().getContentAsString();

		// paginated to show
		// do a GET for events, count should be 0
		// // Search criteria format: ?searchCriteria=name:Special Event

		this.mvc.perform(get("/v1/events?searchCriteria=name:Special Event").with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
				// .andExpect(jsonPath("$..name").value("Special Event"))
				.andDo(document("GetSpecificNamedEvents")).andReturn();

	}

	// Test for multi-filters
	@Test
	public void testAllEventsInALocationThatArePrivate() throws Exception {

		// add a location based event
		String json = "{\"name\": \"Exclusive Event\", " + "\"geoLocation\": \"22,21\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"true\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		result.getResponse().getContentAsString();

		// paginated to show
		// do a GET for events, count should be 0
		// // Search criteria format:
		// ?searchCriteria=location:Chicago,IL;name:Exclusive Event

		this.mvc.perform(get("/v1/events?searchCriteria=location:chicago,il;name:Exclusive Event")
				.with(httpBasic("admin", "admin"))).andExpect(status().isFound())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
				// .andExpect(jsonPath("$..location").value("Chicago, IL"))
				// .andExpect(jsonPath("$..isPublic").value("false"))
				.andDo(document("AllEventsInALocationThatArePrivate")).andReturn();

	}

	// Test for multi-filters
	@Test
	public void testAllEventsInALocationThatArePrivatePaginated() throws Exception {

		for (int i = 0; i < 3; i++) {
			String json = "{\"name\": \"Exclusive Event\", " + "\"geoLocation\": \"22,21\", "
					+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
					+ "\"description\": \"test description\", "
					+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"true\"}";

			this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
					.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		}

		// paginated to show 3 records added above with multi-filters
		// do a GET for events, count should be 0
		// // Search criteria format:
		// ?searchCriteria=location:Chicago,IL;isPublic=false&startIndex=1&pageSize=3

		this.mvc.perform(
				get("/v1/events?searchCriteria=location:chicago,il;name:Exclusive Event&startIndex=1&pageSize=3")
						.with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
				// .andExpect(jsonPath("$..location").value("Chicago, IL"))
				// .andExpect(jsonPath("$..isPublic").value("false"))
				.andDo(document("AllEventsInALocationThatArePrivatePaginated")).andReturn();

	}

	@Test
	public void testForTotalEvents() throws Exception {
		MvcResult result = this.mvc.perform(get("/v1/events/total").with(httpBasic("admin", "admin")))
				.andExpect(status().isOk()).andDo(document("getTotalEvents")).andReturn();
		long a = Long.valueOf(result.getResponse().getContentAsString());

		// add a few events

		String json = "{\"name\": \"testEvent3\", " + "\"geoLocation\": \"123,123\", "
				+ "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", "
				+ "\"description\": \"test description\", "
				+ "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		for (int i = 0; i < 3; i++) {

			result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
					.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

			result.getResponse().getContentAsString();
		}

		result = this.mvc.perform(get("/v1/events/total").with(httpBasic("admin", "admin"))).andExpect(status().isOk())
				.andDo(document("getTotalEvents")).andReturn();
		long b = Long.valueOf(result.getResponse().getContentAsString());

		Assert.assertTrue(b == a + 3);

	}

	/*
	 * Revisit this / Delete and verify record index is updated
	 * 
	 * @Test public void testForDeleteAndVerifyIndexCount() throws Exception {
	 * 
	 * for (int i = 0; i < 3; i++) { String json =
	 * "{\"name\": \"Exclusive Event\", " + "\"geoLocation\": \"22,21\", " +
	 * "\"webUrl\": \"http://localhost/img\", " + "\"photo\": \"img\", " +
	 * "\"description\": \"test description\", " +
	 * "\"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"true\"}"
	 * ;
	 * 
	 * this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON
	 * ).content(json) .with(httpBasic("admin",
	 * "admin"))).andExpect(status().isCreated()).andDo(document("event"))
	 * .andReturn();
	 * 
	 * }
	 * 
	 * // get all records inserted
	 * 
	 * 
	 * 
	 * 
	 * this.mvc.perform(get(
	 * "/v1/events?searchCriteria=location:Chicago,IL;name:Exclusive Event&startIndex=1&pageSize=3"
	 * ) .with(httpBasic("admin", "admin"))).andExpect(status().isFound())
	 * .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3)))) //
	 * .andExpect(jsonPath("$..location").value("Chicago, IL")) //
	 * .andExpect(jsonPath("$..isPublic").value("false"))
	 * .andDo(document("event")).andReturn();
	 * 
	 * }
	 */
}
