package scheduling;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.*;
import java.util.Locale;

public class SchedDataSource {

    final private static SchedDataSource instance = new SchedDataSource();

    //Counters for PKs
    private static int appointmentCounter = 0;
    private static int userCounter = 0;
    private static int customerCounter = 0;
    private static int addressCounter = 0;
    private static int cityCounter = 0;
    private static int countryCounter = 0;

    final private static String loginLog = "loginLog.txt";
    final private static String businessHoursLog = "businessHoursLog.txt";

    private ObservableList<User> allUsers;
    private ObservableList<Appointment> allAppointments;
    private ObservableList<Customer> allCustomers;
    private ObservableList<Address> allAddresses;
    private ObservableList<City> allCities;
    private ObservableList<Country> allCountries;

    private static Connection conn;

    public static int loggedIn = 0;
    public static User currentUser = new User();
    public static Customer currentCustomer = new Customer();

    public static Time businessOpen = Time.valueOf("09:00:00"); //default business hours
    public static Time businessClose = Time.valueOf("17:00:00");

    public static ObservableList<Appointment> thisCustomersAppointments;
    public static Appointment currentAppointment = new Appointment();

    public static Locale currentLocale = Locale.getDefault();

    //String References
    public static final String TABLE_USER = "user";
    public static final String COLUMN_USER_USERID = "userId";
    public static final String COLUMN_USER_USERNAME = "userName";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_ACTIVE = "active";

    //APPOINTMENT TABLE REFERENCES

    public static final String TABLE_APPOINTMENT = "appointment";
    public static final String COLUMN_APPOINTMENT_APPOINTMENTID = "appointmentId";
    public static final String COLUMN_APPOINTMENT_CUSTOMERID = "customerId";
    public static final String COLUMN_APPOINTMENT_USERID = "userId";
    public static final String COLUMN_APPOINTMENT_TITLE = "title";
    public static final String COLUMN_APPOINTMENT_DESCRIPTION = "description";
    public static final String COLUMN_APPOINTMENT_LOCATION = "location";
    public static final String COLUMN_APPOINTMENT_CONTACT = "contact";
    public static final String COLUMN_APPOINTMENT_TYPE = "type";
    public static final String COLUMN_APPOINTMENT_URL = "url";
    public static final String COLUMN_APPOINTMENT_START = "start";
    public static final String COLUMN_APPOINTMENT_END = "end";

    public static final String TABLE_CUSTOMER = "customer";
    public static final String COLUMN_CUSTOMER_CUSTOMERID = "customerId";
    public static final String COLUMN_CUSTOMER_CUSTOMERNAME = "customerName";
    public static final String COLUMN_CUSTOMER_ADDRESSID = "addressId";
    public static final String COLUMN_CUSTOMER_ACTIVE = "active";

    public static final String TABLE_ADDRESS = "address";
    public static final String COLUMN_ADDRESS_ADDRESSID = "addressId";
    public static final String COLUMN_ADDRESS_ADDRESS = "address";
    public static final String COLUMN_ADDRESS_ADDRESS2 = "address2";
    public static final String COLUMN_ADDRESS_CITYID = "cityId";
    public static final String COLUMN_ADDRESS_POSTALCODE = "postalCode";
    public static final String COLUMN_ADDRESS_PHONE = "phone";

    public static final String TABLE_CITY = "city";
    public static final String COLUMN_CITY_CITYID = "cityId";
    public static final String COLUMN_CITY_CITY = "city";
    public static final String COLUMN_CITY_COUNTRYID = "countryId";

    public static final String TABLE_COUNTRY = "country";
    public static final String COLUMN_COUNTRY_COUNTRYID = "countryId";
    public static final String COLUMN_COUNTRY_COUNTRY = "country";

    public static final String COLUMN_CREATEDATE = "createDate";
    public static final String COLUMN_CREATEDBY = "createdBy";
    public static final String COLUMN_LASTUPDATE = "lastUpdate";
    public static final String COLUMN_LASTUPDATEBY = "lastUpdateBy";


    public static SchedDataSource getInstance() {
        return instance;
    }

    public static Connection getConn() {
        return conn;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getLoggedIn() {
        return loggedIn;
    }

    public static void setCustomer(Customer currentCustomer) {
        SchedDataSource.currentCustomer = currentCustomer;
    }

    public static Customer getCustomer() {
        return currentCustomer;
    }

    public static Appointment getCurrentAppointment() {
        return currentAppointment;
    }

    public static void setCurrentAppointment(Appointment currentAppointment) {
        SchedDataSource.currentAppointment = currentAppointment;
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static void setCurrentLocale(Locale locale) {
        SchedDataSource.currentLocale = locale;
    }

    public ObservableList<Appointment> getThisCustomersAppointments() {
        loadThisCustomersAppointments();
        return thisCustomersAppointments;
    }


    public ObservableList<User> getAllUsers() {
        return allUsers;
    }

    public ObservableList<Appointment> getAllAppointments() {
        return allAppointments;
    }

    public ObservableList<Customer> getAllCustomers() {
        return allCustomers;
    }
 //TEST
    public ObservableList<Country> getAllCountries() {
        return allCountries;
    }
    //TEST
    public ObservableList<City> getAllCities(){
        return allCities;
    }
    //TEST
    public ObservableList<Address> getAllAddresses(){
        return allAddresses;
    }

    public static int getUserCounter() {
        return userCounter;
    }

    public static int getCustomerCounter() {
        return customerCounter;
    }

    public static int getAddressCounter() {
        return addressCounter;
    }

    public static int getCityCounter() {
        return cityCounter;
    }

    public static Time getBusinessOpen() {
        LocalDate today = LocalDate.now();
        LocalDateTime ldtStart = LocalDateTime.of(today, businessOpen.toLocalTime());
        LocalDateTime utcStartTime = SchedDataSource.makeThisZone(ldtStart);
        return Time.valueOf(utcStartTime.toLocalTime() + ":00");
    }

    public static void setBusinessOpen(Time businessOpen) {
        SchedDataSource.businessOpen = businessOpen;
    }

    public static Time getBusinessClose() {
        LocalDate today = LocalDate.now();
        LocalDateTime ldtEnd = LocalDateTime.of(today, businessClose.toLocalTime());
        LocalDateTime utcEndTime = SchedDataSource.makeThisZone(ldtEnd);
        return Time.valueOf(utcEndTime.toLocalTime() + ":00");
    }

    public static void setBusinessClose(Time businessClose) {
        SchedDataSource.businessClose = businessClose;
    }

    public void open() {
        //Server Online
        try {
            //Server Connection
            String server = "localhost:3306";
            String db = "schedule";
            String url = "jdbc:mysql://"+server+"/"+db;
            String user = "root";
            String pass = "password";
            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected Online");
        } catch (SQLException e) {
            System.out.println("Couldn't connect to database: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
        }
    }

    public boolean loginUser(String userName, String password) throws Exception {

        Statement statement = getConn().createStatement();

        ResultSet userQuery = statement.executeQuery("SELECT * FROM user");
        while (userQuery.next()) {
            String currentQuery = userQuery.getString("userName");
            if (currentQuery.equalsIgnoreCase(userName)) {
                if (userQuery.getString("password").equals(password)) {
                    SchedDataSource.getCurrentUser().setUserId(Integer.parseInt(userQuery.getString("userId")));
                    SchedDataSource.getCurrentUser().setUserName(userQuery.getString("userName"));
                    SchedDataSource.getCurrentUser().setPassword(password);
                    SchedDataSource.getCurrentUser().setActive(Integer.parseInt(userQuery.getString("active")));
                    SchedDataSource.getCurrentUser().setCreateDate(loadDate(userQuery.getString("createDate")));
                    SchedDataSource.getCurrentUser().setCreatedBy(userQuery.getString("userName"));
                    SchedDataSource.getCurrentUser().setLastUpdate(Timestamp.valueOf(userQuery.getString("lastUpdate")));
                    SchedDataSource.getCurrentUser().setLastUpdateBy(userQuery.getString("userName"));
                    SchedDataSource.setCurrentLocale(Locale.getDefault());

                    statement.close();
                    return true;
                } else {
                    return false;
                }
            }
        }
        statement.close();
        return false;
    }

    public void insertUser(String userName, String password, int active) throws Exception {
        if (userCounter < 2147483647) {
            Statement statement = getConn().createStatement();
            Timestamp timestampNow = new Timestamp(System.currentTimeMillis());
            try {
                userCounter++;
                statement.execute("INSERT INTO " + TABLE_USER
                        + " (" + COLUMN_USER_USERID + ", " +
                        COLUMN_USER_USERNAME + ", " +
                        COLUMN_USER_PASSWORD + ", " +
                        COLUMN_USER_ACTIVE + ", " +
                        COLUMN_CREATEDATE + ", " +
                        COLUMN_CREATEDBY + ", " +
                        COLUMN_LASTUPDATE + ", " +
                        COLUMN_LASTUPDATEBY + ") " +
                        "VALUES ( " + userCounter + ",'" + userName + "', '" + password + "', " + active + ", '"
                        + makeUTC(makeNow()).toString() + "', '" + currentUser.getUserName() + "', '" + makeTimestampUTC(timestampNow) + "', '" + currentUser.getUserName() + "')");
                User newUser = new User(userCounter, userName, password, active, makeNow(), currentUser.getUserName(), timestampNow, currentUser.getUserName());
                allUsers.add(newUser);
                statement.close();
            } catch (SQLException e) {
                userCounter--;
                statement.close();
            }
        } else {
            System.out.println("Too Many Users");
        }
    }

    public void insertAppointment(String title, String description, String location, String contact, String type, String url,
                                  LocalDateTime start, LocalDateTime end) throws Exception {
        if (appointmentCounter < 2147483647) {
            Statement statement = getConn().createStatement();
            Timestamp timestampNow = new Timestamp(System.currentTimeMillis());
            try {
                appointmentCounter++;
                statement.execute("INSERT INTO " + TABLE_APPOINTMENT
                        + " (" + COLUMN_APPOINTMENT_APPOINTMENTID + ", " +
                        COLUMN_APPOINTMENT_CUSTOMERID + ", " +
                        COLUMN_APPOINTMENT_USERID + ", " +
                        COLUMN_APPOINTMENT_TITLE + ", " +
                        COLUMN_APPOINTMENT_DESCRIPTION + ", " +
                        COLUMN_APPOINTMENT_LOCATION + ", " +
                        COLUMN_APPOINTMENT_CONTACT + ", " +
                        COLUMN_APPOINTMENT_TYPE + ", " +
                        COLUMN_APPOINTMENT_URL + ", " +
                        COLUMN_APPOINTMENT_START + ", " +
                        COLUMN_APPOINTMENT_END + ", " +
                        COLUMN_CREATEDATE + ", " +
                        COLUMN_CREATEDBY + ", " +
                        COLUMN_LASTUPDATE + ", " +
                        COLUMN_LASTUPDATEBY + ") " +
                        "VALUES( " + appointmentCounter + ", " + currentCustomer.getCustomerId() + ", " + currentUser.getUserId() + ", '" + title + "', '" + description + "', '"
                        + location + "', '" + contact + "' , '" + type + "', '" + url + "', '" + makeUTC(start) + "', '" + makeUTC(end) + "', '"
                        + makeUTC(makeNow()).toString() + "', '" + currentUser.getUserName() + "', '" + makeTimestampUTC(timestampNow) + "', '" + currentUser.getUserName() + "')");

                Appointment newAppointment = new Appointment(appointmentCounter, currentCustomer.getCustomerId(), currentUser.getUserId(), title, description,
                        location, contact, type, url, start, end, makeNow(), currentUser.getUserName(), timestampNow, currentUser.getUserName());
                statement.close();
                allAppointments.add(newAppointment);
            } catch (SQLException e) {
                appointmentCounter--;
                System.out.println("Failed to Add Appointment");
                statement.close();
            }
        } else {
            System.out.println("Too Many Appointments");
        }
    }

    public void insertCustomer(String customerName, int addressId, int active) throws Exception {
        if (customerCounter < 2147483647) {

            Statement statement = getConn().createStatement();
            Timestamp timestampNow = new Timestamp(System.currentTimeMillis());
            try {
                customerCounter++;
                statement.execute("INSERT INTO " + TABLE_CUSTOMER
                        + " (" + COLUMN_CUSTOMER_CUSTOMERID + ", " +
                        COLUMN_CUSTOMER_CUSTOMERNAME + ", " +
                        COLUMN_CUSTOMER_ADDRESSID + ", " +
                        COLUMN_CUSTOMER_ACTIVE + ", " +
                        COLUMN_CREATEDATE + ", " +
                        COLUMN_CREATEDBY + ", " +
                        COLUMN_LASTUPDATE + ", " +
                        COLUMN_LASTUPDATEBY + ") " +
                        "VALUES( " + customerCounter + ", '" + customerName + "', " + (addressId) + ", " + active + ", '"
                        + makeUTC(makeNow()).toString() + "', '" + currentUser.getUserName() + "', '" + makeTimestampUTC(timestampNow) + "', '" + currentUser.getUserName() + "')");
                statement.close();
            } catch (SQLException e) {
                customerCounter--;
                System.out.println("Failed to Add Customer");
                statement.close();
            }
        } else {
            System.out.println("Too Many Customers");
        }
    }

    public void insertAddress(String address, String address2, int cityId, String postalCode, String phone) throws Exception {
        if (addressCounter < 2147483647) {
            Statement statement = getConn().createStatement();
            Timestamp timestampNow = new Timestamp(System.currentTimeMillis());
            try {
                addressCounter++;
                statement.execute("INSERT INTO " + TABLE_ADDRESS
                        + " (" + COLUMN_ADDRESS_ADDRESSID + ", " +
                        COLUMN_ADDRESS_ADDRESS + ", " +
                        COLUMN_ADDRESS_ADDRESS2 + ", " +
                        COLUMN_ADDRESS_CITYID + ", " +
                        COLUMN_ADDRESS_POSTALCODE + ", " +
                        COLUMN_ADDRESS_PHONE + ", " +
                        COLUMN_CREATEDATE + ", " +
                        COLUMN_CREATEDBY + ", " +
                        COLUMN_LASTUPDATE + ", " +
                        COLUMN_LASTUPDATEBY + ") " +
                        "VALUES( " + addressCounter + ", '" + address + "', '" + address2 + "', " + (cityId) + ", '" + postalCode + "', '" + phone + "' , '"
                        + makeUTC(makeNow()).toString() + "', '" + currentUser.getUserName() + "', '" + makeTimestampUTC(timestampNow) + "', '" + currentUser.getUserName() + "')");
                Address newAddress = new Address(addressCounter, address, address2, cityId, postalCode, phone, makeNow(), currentUser.getUserName(), timestampNow, currentUser.getUserName());
                allAddresses.add(newAddress);
                statement.close();
            } catch (SQLException e) {
                addressCounter--;
                System.out.println("Failed To Add Address");
                statement.close();
            }
        } else {
            System.out.println("Too Many Address'");
        }
    }

    public void insertCity(String city, int countryId) throws Exception {
        if (cityCounter < 2147483647) {

            Statement statement = getConn().createStatement();
            Timestamp timestampNow = new Timestamp(System.currentTimeMillis());
            try {
                cityCounter++;
                statement.execute("INSERT INTO " + TABLE_CITY
                        + " (" + COLUMN_CITY_CITYID + ", " +
                        COLUMN_CITY_CITY + ", " +
                        COLUMN_CITY_COUNTRYID + ", " +
                        COLUMN_CREATEDATE + ", " +
                        COLUMN_CREATEDBY + ", " +
                        COLUMN_LASTUPDATE + ", " +
                        COLUMN_LASTUPDATEBY + ") " +
                        "VALUES( " + cityCounter + ", '" + city + "', " + (countryId) + ", '"
                        + makeUTC(makeNow()).toString() + "', '" + currentUser.getUserName() + "', '" + makeTimestampUTC(timestampNow) + "', '" + currentUser.getUserName() + "')");
                City newCity = new City(cityCounter, city, countryId, makeNow(), currentUser.getUserName(), timestampNow, currentUser.getUserName());
                allCities.add(newCity);
                statement.close();
            } catch (SQLException e) {
                cityCounter--;
                System.out.println("Failed To Add City");
                statement.close();
            }
        } else {
            System.out.println("Too Many Cities");
        }
    }

    public void insertCountry(String country) throws Exception {
        if (countryCounter < 2147483647) {
            Statement statement = getConn().createStatement();
            Timestamp timestampNow = new Timestamp(System.currentTimeMillis());
            try {
                countryCounter++;
                statement.execute("INSERT INTO " + TABLE_COUNTRY
                        + " (" + COLUMN_COUNTRY_COUNTRYID + ", " +
                        COLUMN_COUNTRY_COUNTRY + ", " +
                        COLUMN_CREATEDATE + ", " +
                        COLUMN_CREATEDBY + ", " +
                        COLUMN_LASTUPDATE + ", " +
                        COLUMN_LASTUPDATEBY + ") " +
                        "VALUES( " + countryCounter + ", '" + country + "', '"
                        + makeUTC(makeNow()).toString() + "', '" + currentUser.getUserName() + "', '" + timestampNow + "', '" + currentUser.getUserName() + "')");
                Country newCountry = new Country(countryCounter, country, makeNow(), currentUser.getUserName(), makeTimestampUTC(timestampNow), currentUser.getUserName());
                allCountries.add(newCountry);
                statement.close();
            } catch (SQLException e) {
                countryCounter--;
                System.out.println("Failed To Add Country");
                statement.close();
            }
        } else {
            System.out.println("Too Many Countries");
        }
    }

    public void updateAppointment(int appointmentId, int customerId, int userId, String title, String description, String location, String contact, String type,
                                  String url, LocalDateTime start, LocalDateTime end, LocalDateTime createDate, String createdBy) throws Exception {
        deleteAppointment(appointmentId);
        Statement statement = getConn().createStatement();
        Timestamp timestampNow = new Timestamp(System.currentTimeMillis());
        statement.execute("INSERT INTO " + TABLE_APPOINTMENT
                + " (" + COLUMN_APPOINTMENT_APPOINTMENTID + ", " +
                COLUMN_APPOINTMENT_CUSTOMERID + ", " +
                COLUMN_APPOINTMENT_USERID + ", " +
                COLUMN_APPOINTMENT_TITLE + ", " +
                COLUMN_APPOINTMENT_DESCRIPTION + ", " +
                COLUMN_APPOINTMENT_LOCATION + ", " +
                COLUMN_APPOINTMENT_CONTACT + ", " +
                COLUMN_APPOINTMENT_TYPE + ", " +
                COLUMN_APPOINTMENT_URL + ", " +
                COLUMN_APPOINTMENT_START + ", " +
                COLUMN_APPOINTMENT_END + ", " +
                COLUMN_CREATEDATE + ", " +
                COLUMN_CREATEDBY + ", " +
                COLUMN_LASTUPDATE + ", " +
                COLUMN_LASTUPDATEBY + ") " +
                "VALUES( " + appointmentId + ", " + customerId + ", " + userId + ", '" + title + "', '" + description + "', '"
                + location + "', '" + contact + "' , '" + type + "', '" + url + "', '" + makeUTC(start) + "', '" + makeUTC(end) + "', '"
                + makeUTC(createDate).toString() + "', '" + createdBy + "', '" + makeTimestampUTC(timestampNow) + "', '" + currentUser.getUserName() + "')");
        Appointment newAppointment = new Appointment(appointmentId, customerId, userId, title, description,
                location, contact, type, url, start, end, createDate, createdBy, timestampNow, currentUser.getUserName());
        statement.close();
        allAppointments.add(newAppointment);
    }

    //Insert For Update Customer
    public void reinsertAppointment(Appointment appointment) throws Exception {
        Statement statement = getConn().createStatement();
        Timestamp timestampNow = new Timestamp(System.currentTimeMillis());

        statement.execute("INSERT INTO " + TABLE_APPOINTMENT
                + " (" + COLUMN_APPOINTMENT_APPOINTMENTID + ", " +
                COLUMN_APPOINTMENT_CUSTOMERID + ", " +
                COLUMN_APPOINTMENT_USERID + ", " +
                COLUMN_APPOINTMENT_TITLE + ", " +
                COLUMN_APPOINTMENT_DESCRIPTION + ", " +
                COLUMN_APPOINTMENT_LOCATION + ", " +
                COLUMN_APPOINTMENT_CONTACT + ", " +
                COLUMN_APPOINTMENT_TYPE + ", " +
                COLUMN_APPOINTMENT_URL + ", " +
                COLUMN_APPOINTMENT_START + ", " +
                COLUMN_APPOINTMENT_END + ", " +
                COLUMN_CREATEDATE + ", " +
                COLUMN_CREATEDBY + ", " +
                COLUMN_LASTUPDATE + ", " +
                COLUMN_LASTUPDATEBY + ") " +
                "VALUES( " + appointment.getAppointmentId() + ", " + appointment.getCustomerId() + ", " + appointment.getUserId() + ", '" + appointment.getTitle() + "', '" + appointment.getDescription() + "', '"
                + appointment.getLocation() + "', '" + appointment.getContact() + "' , '" + appointment.getType() + "', '" + appointment.getUrl() + "', '" + makeUTC(appointment.getStart()) + "', '" + makeUTC(appointment.getEnd()) + "', '"
                + makeUTC(appointment.getCreateDate()) + "', '" + appointment.getCreatedBy() + "', '" + makeTimestampUTC(timestampNow) + "', '" + currentUser.getUserName() + "')");

        Appointment newAppointment = new Appointment(appointment.getAppointmentId(), appointment.getCustomerId(), appointment.getUserId(), appointment.getTitle(),
                appointment.getDescription(), appointment.getLocation(), appointment.getContact(), appointment.getType(),
                appointment.getUrl(), appointment.getStart(), appointment.getEnd(), appointment.getCreateDate(), appointment.getCreatedBy(), timestampNow, currentUser.getUserName());
        statement.close();
        allAppointments.add(newAppointment);
    }

    public Customer updateCustomer(int customerId, String customerName, int addressId, int active, LocalDateTime createDate, String createdBy, ObservableList<Appointment> theseAppointments) throws Exception {

        Statement statement = getConn().createStatement();
        Timestamp timestampNow = new Timestamp(System.currentTimeMillis());

        //Reinsert Customer
        statement.execute("INSERT INTO " + TABLE_CUSTOMER
                + " (" + COLUMN_CUSTOMER_CUSTOMERID + ", " +
                COLUMN_CUSTOMER_CUSTOMERNAME + ", " +
                COLUMN_CUSTOMER_ADDRESSID + ", " +
                COLUMN_CUSTOMER_ACTIVE + ", " +
                COLUMN_CREATEDATE + ", " +
                COLUMN_CREATEDBY + ", " +
                COLUMN_LASTUPDATE + ", " +
                COLUMN_LASTUPDATEBY + ") " +
                "VALUES( " + customerId + ", '" + customerName + "', " + addressId + ", " + active + ", '"
                + makeUTC(createDate).toString() + "', '" + createdBy + "', '" + makeTimestampUTC(timestampNow) + "', '" + currentUser.getUserName() + "')");
        Customer updatedCustomer = new Customer(customerId, customerName,
                addressId, active, createDate, createdBy, timestampNow, SchedDataSource.getCurrentUser().getUserName());
        allCustomers.add(updatedCustomer);

        //Reinsert Customers Appointments
        for (Appointment appointment : theseAppointments) {
            reinsertAppointment(appointment);
        }

        statement.close();
        return updatedCustomer;
    }

    public int updateAddress(int addressId, String address, String address2, int cityId, String postalCode, String phone, LocalDateTime createDate, String createdBy) throws Exception {

        Statement statement = getConn().createStatement();
        Timestamp timestampNow = new Timestamp(System.currentTimeMillis());

        //Go through all addresses
        for (Address tempAddress : allAddresses) {
            //and if there is an address with the same info
            if (tempAddress.getAddressId() != addressId && address.equalsIgnoreCase(tempAddress.getAddress()) && address2.equalsIgnoreCase(tempAddress.getAddress2())
                    && postalCode.equalsIgnoreCase(tempAddress.getPostalCode()) && phone.equalsIgnoreCase(tempAddress.getPhone()) && tempAddress.getCityId() == cityId) {
                System.out.println("Address already exists, linking...");
                //use that instead
                int existingAddressId = tempAddress.getAddressId();
                statement.close();
                return existingAddressId;
            }
        }
        statement.execute("INSERT INTO " + TABLE_ADDRESS
                + " (" + COLUMN_ADDRESS_ADDRESSID + ", " +
                COLUMN_ADDRESS_ADDRESS + ", " +
                COLUMN_ADDRESS_ADDRESS2 + ", " +
                COLUMN_ADDRESS_CITYID + ", " +
                COLUMN_ADDRESS_POSTALCODE + ", " +
                COLUMN_ADDRESS_PHONE + ", " +
                COLUMN_CREATEDATE + ", " +
                COLUMN_CREATEDBY + ", " +
                COLUMN_LASTUPDATE + ", " +
                COLUMN_LASTUPDATEBY + ") " +
                "VALUES( " + addressId + ", '" + address + "', '" + address2 + "', " + cityId + ", '" + postalCode + "', '" + phone + "' , '"
                + makeUTC(createDate).toString() + "', '" + createdBy + "', '" + makeTimestampUTC(timestampNow) + "', '" + currentUser.getUserName() + "')");

        Address newAddress = new Address(addressId, address, address2, cityId, postalCode, phone, createDate, createdBy, timestampNow, currentUser.getUserName());
        allAddresses.add(newAddress);
        statement.close();
        return newAddress.getAddressId();
    }

    public int updateCity(int cityId, String city, int countryId, LocalDateTime createDate, String createdBy) throws Exception {
        Statement statement = getConn().createStatement();
        Timestamp timestampNow = new Timestamp(System.currentTimeMillis());
        //Check if city exists, if so return its CityID
        for (City tempCity : allCities) {
            if (tempCity.getCity().equalsIgnoreCase(city) && tempCity.getCityId() != cityId && tempCity.getCountryId() == countryId) {
                int existingCityId = tempCity.getCityId();
                System.out.println("City already exits, linking...");
                statement.close();
                return existingCityId;
            }
        }
        statement.execute("INSERT INTO " + TABLE_CITY
                + " (" + COLUMN_CITY_CITYID + ", " +
                COLUMN_CITY_CITY + ", " +
                COLUMN_CITY_COUNTRYID + ", " +
                COLUMN_CREATEDATE + ", " +
                COLUMN_CREATEDBY + ", " +
                COLUMN_LASTUPDATE + ", " +
                COLUMN_LASTUPDATEBY + ") " +
                "VALUES( " + cityId + ", '" + city + "', " + countryId + ", '"
                + makeUTC(createDate).toString() + "', '" + createdBy + "', '" + makeTimestampUTC(timestampNow) + "', '" + currentUser.getUserName() + "')");

        City newCity = new City(cityId, city, countryId, createDate, createdBy, timestampNow, currentUser.getUserName());
        allCities.add(newCity);
        statement.close();
        return newCity.getCityId();
    }

    public int updateCountry(int countryId, String country, LocalDateTime createDate, String createdBy) throws Exception {
        Statement statement = getConn().createStatement();
        Timestamp timestampNow = new Timestamp(System.currentTimeMillis());

        for (Country tempCountry : allCountries) {
            if (tempCountry.getCountry().equalsIgnoreCase(country) && countryId != tempCountry.getCountryId()) {
                int existingId = tempCountry.getCountryId();
                System.out.println("Country already exists, linking...");
                statement.close();
                return existingId;
            }
        }
        statement.execute("INSERT INTO " + TABLE_COUNTRY
                + " (" + COLUMN_COUNTRY_COUNTRYID + ", " +
                COLUMN_COUNTRY_COUNTRY + ", " +
                COLUMN_CREATEDATE + ", " +
                COLUMN_CREATEDBY + ", " +
                COLUMN_LASTUPDATE + ", " +
                COLUMN_LASTUPDATEBY + ") " +
                "VALUES( " + countryId + ", '" + country + "', '"
                + makeUTC(createDate).toString() + "', '" + createdBy + "', '" + makeTimestampUTC(timestampNow) + "', '" + currentUser.getUserName() + "')");
        Country newCountry = new Country(countryId, country, createDate, createdBy, timestampNow, currentUser.getUserName());
        allCountries.add(newCountry);
        statement.close();
        return newCountry.getCountryId();
    }

    public void deleteAppointment(int appointmentId) throws Exception {
                Statement statement = conn.createStatement();
                statement.execute("DELETE FROM " + TABLE_APPOINTMENT + " WHERE " + COLUMN_APPOINTMENT_APPOINTMENTID + "= '" + appointmentId + "'");
                allAppointments.remove(getAppointment(appointmentId));
                statement.close();
    }

    public void deleteCustomersAppointments() throws Exception {

        ObservableList<Appointment> allAppointments = getThisCustomersAppointments();
        if (allAppointments != null) {
            for (Appointment appointment : allAppointments) {
                deleteAppointment(appointment.getAppointmentId());
            }
        }

    }

    public void deleteCustomer(int customerId) throws Exception {
        deleteCustomersAppointments();
        Statement statement = conn.createStatement();
        statement.execute("DELETE FROM " + TABLE_CUSTOMER + " WHERE " + COLUMN_CUSTOMER_CUSTOMERID + "= '" + customerId + "'");
        allCustomers.remove(getCustomer(customerId));
        statement.close();
    }

    public boolean deleteAddress(int addressId) {
        try {
            Statement statement = conn.createStatement();
            statement.execute("DELETE FROM " + TABLE_ADDRESS + " WHERE " + COLUMN_ADDRESS_ADDRESSID + "= '" + addressId + "'");
            allAddresses.remove(getAddress(addressId));
            statement.close();

        } catch (SQLException E) {
            return false;
        }
        return true;
    }

    public boolean deleteCity(int cityId) {
        try {
            Statement statement = conn.createStatement();
            statement.execute("DELETE FROM " + TABLE_CITY + " WHERE " + COLUMN_CITY_CITYID + "= '" + cityId + "'");
            allCities.remove(getCity(cityId));
            statement.close();

        } catch (SQLException E) {
            return false;
        }
        return true;
    }

    public boolean deleteCountry(int countryId) {
        try {
            Statement statement = conn.createStatement();
            statement.execute("DELETE FROM " + TABLE_COUNTRY + " WHERE " + COLUMN_COUNTRY_COUNTRYID + "= '" + countryId + "'");
            allCountries.remove(getCountry(countryId));
            statement.close();
        } catch (SQLException E) {
            System.out.println(E);
            return false;
        }
        return true;
    }

    public boolean findUserName(String userName) {
//Searching if User Exists
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getUserName().equalsIgnoreCase(userName)) {
                //User Already Exists"
                return false;
            }
        }
        return true;
    }

    public int findAddressId(String address, String address2, int cityId, String postalCode, String phone) throws Exception {
        //Searching if Address Exists
        for (Address tempAddress : allAddresses) {
            if (tempAddress.getAddress().equalsIgnoreCase(address)) {
                if (tempAddress.getPostalCode().equalsIgnoreCase(postalCode)) {
                    if (tempAddress.getPhone().equalsIgnoreCase(phone)) {
                        if (tempAddress.getAddress2().equalsIgnoreCase(address2) && cityId == tempAddress.getCityId()) {
                            System.out.println("Address2 Matches & CityID Matches");
                            return tempAddress.getAddressId();
                        } else if (address2.equalsIgnoreCase(address)) {
                            System.out.println("Saved Address2 is same as Address1 (Dummy)");
                        } else {
                            System.out.println("No Matches");
                        }
                    }
                }
            }
        }
        System.out.println("No Matches Outside Loop");
        SchedDataSource.getInstance().insertAddress(address, address2, cityId, postalCode, phone);
        return SchedDataSource.getAddressCounter();
    }

    public int findCityId(String city, int countryId) throws Exception {
        System.out.println("Searching if City Exists");
        for (int i = 0; i < allCities.size(); i++) {
            if (allCities.get(i).getCity().equalsIgnoreCase(city) && countryId == allCities.get(i).getCountryId()) {
                System.out.println("City Exists");
                return allCities.get(i).getCityId();
            }
        }
        SchedDataSource.getInstance().insertCity(city, countryId);
        System.out.println("No city found, Returning " + (cityCounter) + " as newly created city's CityID for FindAddress");
        return SchedDataSource.getCityCounter();
    }

    public int findCountryId(String country) throws Exception {
        //Search for if Country Exists
        for (int i = 0; i < allCountries.size(); i++) {
            if (allCountries.get(i).getCountry().equalsIgnoreCase(country)) {
                System.out.println("Country Exists");
                return allCountries.get(i).getCountryId();
            }
        }
        SchedDataSource.getInstance().insertCountry(country);
        System.out.println("Country DNE");
        return countryCounter;
    }

    public void loadUsers() throws Exception {
        allUsers = FXCollections.observableArrayList();
        Statement statement = getConn().createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM user");
        while (rs.next()) {
            User user = new User(Integer.parseInt(rs.getString("userId")), rs.getString("userName"), rs.getString("password"),
                    Integer.parseInt(rs.getString("active")), loadDate(rs.getString("createDate")),
                    rs.getString("createdBy"), makeTimestampThisZone(Timestamp.valueOf(rs.getString("lastUpdate"))),
                    rs.getString("lastUpdateBy"));
            allUsers.add(user);
        }

        if (allUsers.size() == 0) {
            checkAdminExists();
        }
        statement.close();
    }

    public void loadAppointments() throws Exception {
        allAppointments = FXCollections.observableArrayList();
        Statement statement = getConn().createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM appointment");
        while (rs.next()) {
            Appointment appointment = new Appointment(
                    Integer.parseInt(rs.getString("appointmentId")),
                    Integer.parseInt(rs.getString("customerId")),
                    Integer.parseInt(rs.getString("userId")),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("location"),
                    rs.getString("contact"),
                    rs.getString("type"),
                    rs.getString("url"),

                    loadDate(rs.getString("start")),
                    loadDate(rs.getString("end")),
                    loadDate(rs.getString("createDate")),

                    rs.getString("createdBy"), makeTimestampThisZone(Timestamp.valueOf(rs.getString("lastUpdate"))),
                    rs.getString("lastUpdateBy"));

            allAppointments.add(appointment);
        }
        statement.close();
    }

    public void loadCustomers() throws Exception {
        allCustomers = FXCollections.observableArrayList();
        Statement statement = getConn().createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM customer");
        while (rs.next()) {
            Customer customer = new Customer(Integer.parseInt(rs.getString("customerId")), rs.getString("customerName"),
                    Integer.parseInt(rs.getString("addressId")), Integer.parseInt(rs.getString("active")), loadDate(rs.getString("createDate")),
                    rs.getString("createdBy"), makeTimestampThisZone(Timestamp.valueOf(rs.getString("lastUpdate"))), rs.getString("lastUpdateBy"));

            allCustomers.add(customer);
        }
        statement.close();
    }

    public void loadAddresses() throws Exception {
        allAddresses = FXCollections.observableArrayList();
        Statement statement = getConn().createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM address");
        while (rs.next()) {
            Address address = new Address(Integer.parseInt(rs.getString("addressId")), rs.getString("address"), rs.getString("address2"),
                    Integer.parseInt(rs.getString("cityId")), rs.getString("postalCode"), rs.getString("phone"), loadDate(rs.getString("createDate")),
                    rs.getString("createdBy"), makeTimestampThisZone(Timestamp.valueOf(rs.getString("lastUpdate"))), rs.getString("lastUpdateBy"));

            allAddresses.add(address);
        }
        statement.close();
    }

    public void loadCities() throws Exception {
        allCities = FXCollections.observableArrayList();
        Statement statement = getConn().createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM city");
        while (rs.next()) {
            City city = new City(Integer.parseInt(rs.getString("cityId")), rs.getString("city"),
                    Integer.parseInt(rs.getString("countryId")), loadDate(rs.getString("createDate")),
                    rs.getString("createdBy"), makeTimestampThisZone(Timestamp.valueOf(rs.getString("lastUpdate"))), rs.getString("lastUpdateBy"));

            allCities.add(city);
        }
        statement.close();
    }

    public void loadCountries() throws Exception {
        allCountries = FXCollections.observableArrayList();
        Statement statement = getConn().createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM country");
        while (rs.next()) {
            Country country = new Country(Integer.parseInt(rs.getString("countryId")),
                    rs.getString("country"), loadDate(rs.getString("createDate")),
                    rs.getString("createdBy"), makeTimestampThisZone(Timestamp.valueOf(rs.getString("lastUpdate"))), rs.getString("lastUpdateBy"));

            allCountries.add(country);
        }
        statement.close();
    }

    public void loadLoaders() throws Exception {
        loadCountries();
        loadCities();
        loadAddresses();
        loadCustomers();
        loadAppointments();
        loadCounters();
        loggedIn = 1;
    }

    public void loadCounters() {
        for (User user : allUsers) {
            if (user.getUserId() > userCounter) {
                userCounter = user.getUserId();
            }
        }

        for (Appointment appointment : allAppointments) {
            if (appointment.getAppointmentId() > appointmentCounter) {
                appointmentCounter = appointment.getAppointmentId();
            }
        }

        for (Customer customer : allCustomers) {
            customerCounter++;
            if (customer.getCustomerId() > customerCounter) {
                customerCounter = customer.getCustomerId();
            }
        }

        for (Address address : allAddresses) {
            if (address.getAddressId() > addressCounter) {
                addressCounter = address.getAddressId();
            }
        }

        for (City city : allCities) {
            if (city.getCityId() > cityCounter) {
                cityCounter = city.getCityId();
            }
        }

        for (Country country : allCountries) {
            if (country.getCountryId() > countryCounter) {
                countryCounter = country.getCountryId();
            }
        }
    }

    public void checkAdminExists() throws Exception {
        Statement statement = getConn().createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM user");
        while (rs.next()) {
            if (rs.getString("userName").equalsIgnoreCase("test")) {
                //Test Account Exists
            }
        }
        System.out.println("Test account doesn't exist yet... First Run, Welcome");

        Timestamp timestampNow = new Timestamp(System.currentTimeMillis());
        statement.execute("INSERT INTO " + TABLE_USER
                + " (" + COLUMN_USER_USERID + ", " +
                COLUMN_USER_USERNAME + ", " +
                COLUMN_USER_PASSWORD + ", " +
                COLUMN_USER_ACTIVE + ", " +
                COLUMN_CREATEDATE + ", " +
                COLUMN_CREATEDBY + ", " +
                COLUMN_LASTUPDATE + ", " +
                COLUMN_LASTUPDATEBY + ") " +
                "VALUES ( " + userCounter + ", 'Test' , 'test' , 1, '"
                + getCurrentDateTime() + "', '" + currentUser.getUserName() + "', '" + makeTimestampUTC(timestampNow) + "', '" + currentUser.getUserName() + "')");

        User user = new User(SchedDataSource.getUserCounter(), "Test",
                " ", 0, makeNow(), SchedDataSource.getCurrentUser().getUserName(), timestampNow, SchedDataSource.getCurrentUser().getUserName());
        SchedDataSource.getInstance().getAllUsers().add(user);
        userCounter++;
        statement.close();
    }

    public static LocalDateTime loadDate(String sqlDate) {
        StringBuilder now = new StringBuilder(sqlDate);
        int year = Integer.parseInt(now.substring(0, 4));
        now.delete(0, 5);
        int month = Integer.parseInt(now.substring(0, 2));
        now.delete(0, 3);
        int dayOfMonth = Integer.parseInt(now.substring(0, 2));
        now.delete(0, 3);
        int hour = Integer.parseInt(now.substring(0, 2));
        now.delete(0, 3);
        int minute = Integer.parseInt(now.substring(0, 2));
        now.delete(0, 3);
        int second = Integer.parseInt(now.substring(0, 2));
        now.delete(0, (now.indexOf("[")) + 1).delete(now.length() - 1, now.length());
        LocalDateTime thisDate = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, 0);

        return makeThisZone(thisDate);
    }

    public String getCurrentDateTime() {
        LocalDateTime dateTime = makeNow();
        return dateTime.toString();
    }

    public String getTime(LocalDateTime aDateTime) {
        StringBuilder now = new StringBuilder(aDateTime.toString());
        now.delete(10, 11).insert(10, ' ');
        return now.toString();
    }

    public boolean checkBusinessHours(LocalDateTime start, LocalDateTime end) {
        LocalDate today = start.toLocalDate();
        LocalDateTime ldtStart = LocalDateTime.of(today, businessOpen.toLocalTime());
        LocalDateTime utcStartTime = SchedDataSource.makeThisZone(ldtStart);

        LocalDateTime ldtEnd = LocalDateTime.of(today, businessClose.toLocalTime());
        LocalDateTime utcEndTime = SchedDataSource.makeThisZone(ldtEnd);

        if (utcStartTime.isAfter(utcEndTime))
            utcEndTime = utcEndTime.plusDays(1);

        return ((utcStartTime.isBefore(start) || utcStartTime.equals(start)) && utcEndTime.isAfter(end));
    }

    public boolean checkUserAvailability(int userId, LocalDateTime start, LocalDateTime end) {

        for (User user : allUsers) {
            if (userId == user.getUserId()) {
                for (Appointment appointment : loadThisUsersAppointments(user.getUserId())) {
                    if (datesOverlapping(start, end, appointment.getStart(), appointment.getEnd())) {
                        System.out.println("User Has Overlapping Appointments");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean checkCustomerAvailability(int customerId, LocalDateTime start, LocalDateTime end) {

        for (Customer customer : allCustomers) {
            if (customerId == customer.getCustomerId()) {
                for (Appointment appointment : loadThisUsersAppointments(customer.getCustomerId())) {
                    if (datesOverlapping(start, end, appointment.getStart(), appointment.getEnd())) {
//                        System.out.println("Customer Has Overlapping Appointments");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean checkUserAvailability(int appointmentId, int userId, LocalDateTime start, LocalDateTime end) {

        for (User user : allUsers) {
            if (userId == user.getUserId()) {
                for (Appointment appointment : loadThisUsersAppointments(user.getUserId())) {
                    if (datesOverlapping(start, end, appointment.getStart(), appointment.getEnd()) && appointmentId != appointment.getAppointmentId()) {
//                        System.out.println("User Has Overlapping Appointments");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean checkCustomerAvailability(int appointmentId, int customerId, LocalDateTime start, LocalDateTime end) {

        for (Customer customer : allCustomers) {
            if (customerId == customer.getCustomerId()) {
                for (Appointment appointment : loadThisUsersAppointments(customer.getCustomerId())) {
                    if (datesOverlapping(start, end, appointment.getStart(), appointment.getEnd()) && appointmentId != appointment.getAppointmentId()) {
//                        System.out.println("Customer Has Overlapping Appointments");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean datesOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return (start1.isBefore(end2) && start2.isBefore(end1) || (start1.equals(end2) || start2.equals(end1)));
    }

    public void logUserLogin() throws IOException {

        Timestamp timestampNow = new Timestamp(System.currentTimeMillis());
        timestampNow = makeTimestampUTC(timestampNow);
        File file = new File(loginLog);
        FileWriter fileWriter = new FileWriter(file, true);
        BufferedWriter br = new BufferedWriter(fileWriter);

        try {
            System.out.println(" Recording Login @ UTC Time...");
        } finally {
            br.write(String.format("User: %s logged in at : %s\n", currentUser.getUserName(), makeTimestampUTC(timestampNow)));
            br.close();
            fileWriter.close();
            br.close();
        }
    }

    public void logBusinessHours() throws IOException {
        File file = new File(businessHoursLog);
        FileWriter fileWriter = new FileWriter(file, false);
        BufferedWriter br = new BufferedWriter(fileWriter);

        try {
            System.out.println("Recording Business Hours...");
        } finally {
            br.write(businessOpen + "\n" + businessClose);
            br.close();
            fileWriter.close();
            br.close();
        }
    }

    public void loadBusinessHours() {
        Path path = Paths.get(businessHoursLog);
        try {
            BufferedReader br = Files.newBufferedReader(path);

            setBusinessOpen(Time.valueOf(br.readLine()));
            setBusinessClose(Time.valueOf(br.readLine()));
            br.close();
        } catch (IOException E) {
            //Business Hours Never Set, Default 9AM - 5PM UTC
        }
    }

    public String getCustomerInfo() {

        StringBuilder customerData = new StringBuilder();
        Address currentAddress = new Address();
        City currentCity = new City();
        Country currentCountry = new Country();

        int customerId = getCustomer().getCustomerId();
        int addressId = getCustomer().getAddressId();
        String customerName = getCustomer().getCustomerName();
        customerData.append(customerName).append("\n");
        for (Address address : allAddresses) {
            if (address.getAddressId() == addressId) {
                currentAddress = address;
            }
        }
        String address = currentAddress.getAddress();
        String address2 = currentAddress.getAddress2();
        int cityId = currentAddress.getCityId();
        String postalCode = currentAddress.getPostalCode();
        String phone = currentAddress.getPhone();

        if (address.equalsIgnoreCase(address2)) {
            customerData.append(address).append("\n");
        } else {
            customerData.append(address).append("\n").append(address2).append("\n");
        }

        for (City city : allCities) {
            if (city.getCityId() == cityId) {
                currentCity = city;
            }
        }
        String city = currentCity.getCity();
        int countryId = currentCity.getCountryId();

        for (Country country : allCountries) {
            if (country.getCountryId() == countryId) {
                currentCountry = country;
            }
        }
        String country = currentCountry.getCountry();
        customerData.append(city).append(", ").append(country).append(", ").append(postalCode).append("\nPhone: ").append(phone);
        customerData.append("\nCustomerID:").append(customerId).append(" AdID:").append(addressId).append(" CityID:").append(cityId).append(" CountryID:").append(countryId);
        return customerData.toString();
    }

    public void loadThisCustomersAppointments() {
        if (currentCustomer != null) {
            if (!allAppointments.isEmpty()) {
                ObservableList<Appointment> theseAppointments = FXCollections.observableArrayList();
                for (Appointment appointment : allAppointments) {
                    if (appointment.getCustomerId() == currentCustomer.getCustomerId()) {
                        theseAppointments.add(appointment);
                    }
                }
                thisCustomersAppointments = theseAppointments;
            } else {
                thisCustomersAppointments = null;
            }
        } else {
            thisCustomersAppointments = null;
        }
    }

    public ObservableList<Appointment> userSchedule() {
        return loadThisUsersAppointments(currentUser.getUserId());
    }

    public ObservableList<Appointment> loadThisUsersAppointments(int userId) {

        ObservableList<Appointment> usersAppointments = FXCollections.observableArrayList();
        for (Appointment appointment : allAppointments) {
            if (appointment.getUserId() == userId) {
                usersAppointments.add(appointment);
            }
        }
        return usersAppointments;
    }

    public String getAppointmentInfo() {

        StringBuilder appointmentData = new StringBuilder();
        Appointment thisAppointment = currentAppointment;

        String title = thisAppointment.getTitle();
        int appointmentId = thisAppointment.getAppointmentId();
        String description = thisAppointment.getDescription();
        String location = thisAppointment.getLocation();
        String contact = thisAppointment.getContact();
        String type = thisAppointment.getType();
        String url = thisAppointment.getUrl();
        String createdBy = thisAppointment.getCreatedBy();
        String createDate = thisAppointment.getCreateDate().toString();
        String lastUpdated = thisAppointment.getLastUpdateBy();
        LocalDateTime aStartDate = thisAppointment.getStart();
        LocalDateTime anEndDate = thisAppointment.getEnd();
        String timestamp = thisAppointment.getLastUpdate().toString();

        appointmentData.append("Title: ").append(title);
        appointmentData.append("    Appointment ID: ").append(appointmentId).append("\n\n");
        appointmentData.append("Start Date: ").append(getTime(aStartDate)).append("\n");
        appointmentData.append("End Date:   ").append(getTime(anEndDate)).append("\n\n");
        appointmentData.append("Type: ").append(type).append("\n");
        appointmentData.append("Description: ").append(description).append("\n");
        appointmentData.append("Location: ").append(location).append("\n\n");
        appointmentData.append("Contact: ").append(contact).append("\n");
        appointmentData.append("URL: ").append(url).append("\n\n");
        appointmentData.append("Created By: ").append(createdBy).append("\n");
        appointmentData.append("Create Date: ").append(createDate).append("\n\n");
        appointmentData.append("Last Updated By: ").append(lastUpdated).append("\n");
        appointmentData.append("Last Updated: ").append(timestamp);

        return appointmentData.toString();
    }

    private Appointment getAppointment(int appointmentId) {
        for (Appointment appointment : allAppointments) {
            if (appointmentId == appointment.getAppointmentId())
                return appointment;
        }
        return new Appointment();
    }

    public Customer getCustomer(int customerId) {
        for (Customer customer : allCustomers) {
            if (customerId == customer.getCustomerId())
                return customer;
        }

        return new Customer();
    }

    public Address getAddress(int addressId) {
        for (Address address : allAddresses) {
            if (addressId == address.getAddressId())
                return address;
        }
        return new Address();
    }

    public City getCity(int cityId) {
        for (City city : allCities) {
            if (cityId == city.getCityId())
                return city;
        }
        return new City();
    }

    public Country getCountry(int countryId) {
        for (Country country : allCountries) {
            if (countryId == country.getCountryId())
                return country;
        }
        return new Country();
    }

    public void findWastedIDs() {
        int wastedAppointmentIds = appointmentCounter - allAppointments.size();
        int wastedCustomerIds = customerCounter - allCustomers.size();
        int wastedAddressIds = addressCounter - allAddresses.size();
        int wastedCityIds = cityCounter - allCities.size();
        int wastedCountryIds = countryCounter - allCountries.size();

        System.out.println("Wasted Primary Keys\n************");
        System.out.print("Appointment: ");
        for (int i = 1; i < appointmentCounter; i++) {
//            System.out.println("\n"+getCustomer(i).getCustomerName());
            if (getAppointment(i).getTitle() == null) {
                System.out.print(i + ", ");
            }
        }
        System.out.print("(" + wastedAppointmentIds + ") Wasted Total\nCustomer: ");
        for (int i = 1; i < customerCounter; i++) {
//            System.out.println("\n"+getAddress(i).getAddress());
            if (getCustomer(i).getCustomerName() == null)
                System.out.print(i + ", ");
        }
        System.out.print("(" + wastedCustomerIds + ") Wasted Total\nAddress: ");
        for (int i = 1; i < addressCounter; i++) {
//            System.out.println("\n"+getAddress(i).getAddress());
            if (getAddress(i).getAddress() == null)
                System.out.print(i + ", ");
        }
        System.out.print("(" + wastedAddressIds + ") Wasted Total\nCity: ");
        for (int i = 1; i < cityCounter; i++) {
//            System.out.println("\n"+getCity(i).getCity());
            if (getCity(i).getCity() == null)
                System.out.print(i + ", ");
        }
        System.out.print("(" + wastedCityIds + ") Wasted Total\nCountry: ");
        for (int i = 1; i < countryCounter; i++) {
//            System.out.println("\n"+getCountry(i).getCountry());
            if (getCountry(i).getCountry() == null)
                System.out.print(i + ", ");
        }
        System.out.print("(" + wastedCountryIds + ") Wasted Total\n");

    }

    public LocalDateTime makeNow() {
        LocalDateTime ldt = LocalDateTime.now();
        ZonedDateTime ldtZoned = ldt.atZone(ZoneId.systemDefault());
        ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));

        StringBuilder date = new StringBuilder(utcZoned.toString());

        int year = Integer.parseInt(date.substring(0, date.indexOf("-")));
        date.delete(0, date.indexOf("-") + 1);
        int month = Integer.parseInt(date.substring(0, date.indexOf("-")));
        date.delete(0, date.indexOf("-") + 1);
        int day = Integer.parseInt(date.substring(0, date.indexOf("T")));
        date.delete(0, date.indexOf("T") + 1);
        int hour = Integer.parseInt(date.substring(0, date.indexOf(":")));
        date.delete(0, date.indexOf(":") + 1);
        int minute = Integer.parseInt(date.substring(0, date.indexOf(":")));
        date.delete(0, date.indexOf(":") + 1);

        return LocalDateTime.of(year, month, day, hour, minute, 0, 0);
    }

    public LocalDateTime makeUTC(LocalDateTime ldt) {
        ZonedDateTime ldtZoned = ldt.atZone(ZoneId.systemDefault());
        ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));

        StringBuilder date = new StringBuilder(utcZoned.toString());

        int year = Integer.parseInt(date.substring(0, date.indexOf("-")));
        date.delete(0, date.indexOf("-") + 1);
        int month = Integer.parseInt(date.substring(0, date.indexOf("-")));
        date.delete(0, date.indexOf("-") + 1);
        int day = Integer.parseInt(date.substring(0, date.indexOf("T")));
        date.delete(0, date.indexOf("T") + 1);
        int hour = Integer.parseInt(date.substring(0, date.indexOf(":")));
        date.delete(0, date.indexOf(":") + 1);
        int minute = Integer.parseInt(date.substring(0, 2));

        return LocalDateTime.of(year, month, day, hour, minute, 0, 0);
    }

    public static LocalDateTime makeThisZone(LocalDateTime ldt) {
        ZonedDateTime utcZoned = ldt.atZone(ZoneId.of("UTC"));
        ZonedDateTime ldtZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault());

        StringBuilder date = new StringBuilder(ldtZoned.toString());

        int year = Integer.parseInt(date.substring(0, date.indexOf("-")));
        date.delete(0, date.indexOf("-") + 1);
        int month = Integer.parseInt(date.substring(0, date.indexOf("-")));
        date.delete(0, date.indexOf("-") + 1);
        int day = Integer.parseInt(date.substring(0, date.indexOf("T")));
        date.delete(0, date.indexOf("T") + 1);
        int hour = Integer.parseInt(date.substring(0, date.indexOf(":")));
        date.delete(0, date.indexOf(":") + 1);
        int minute = Integer.parseInt(date.substring(0, 2));
        date.delete(0, 2);

        return LocalDateTime.of(year, month, day, hour, minute, 0, 0);
    }

    public Timestamp makeTimestampUTC(Timestamp timestamp) {
        StringBuilder millisecondsString = new StringBuilder(timestamp.toString());
        millisecondsString.delete(0, millisecondsString.indexOf(".") + 1);
        int milliseconds = Integer.parseInt(millisecondsString.substring(0, millisecondsString.length())) * 1000000;

        ZonedDateTime ldtZoned = timestamp.toLocalDateTime().atZone(ZoneId.systemDefault());
        ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));

        StringBuilder date = new StringBuilder(utcZoned.toString());
        int year = Integer.parseInt(date.substring(0, date.indexOf("-")));
        date.delete(0, date.indexOf("-") + 1);
        int month = Integer.parseInt(date.substring(0, date.indexOf("-")));
        date.delete(0, date.indexOf("-") + 1);
        int day = Integer.parseInt(date.substring(0, date.indexOf("T")));
        date.delete(0, date.indexOf("T") + 1);
        int hour = Integer.parseInt(date.substring(0, date.indexOf(":")));
        date.delete(0, date.indexOf(":") + 1);

        if (date.toString().contains("+")){
            date.delete(date.indexOf("+"), date.length());
        } else if (date.toString().contains("-")){
            date.delete(date.indexOf("-"), date.length());
        } else {
            date.delete(date.indexOf("Z"), date.length());
        }

        if(date.toString().contains(":")){
            int minute = Integer.parseInt(date.substring(0, date.indexOf(":")));
            date.delete(0, date.indexOf(":") + 1);
            int seconds = Integer.parseInt(date.substring(0, 2));
            return Timestamp.valueOf(LocalDateTime.of(year, month, day, hour, minute, seconds, milliseconds));
        } else{
            int minute = Integer.parseInt(date.substring(0, 2));
            return Timestamp.valueOf(LocalDateTime.of(year, month, day, hour, minute, 0, milliseconds));
        }
    }

    public Timestamp makeTimestampThisZone(Timestamp timestamp) {
        StringBuilder millisecondsString = new StringBuilder(timestamp.toString());
        millisecondsString.delete(0, millisecondsString.indexOf(".") + 1);
        int milliseconds = Integer.parseInt(millisecondsString.substring(0, millisecondsString.length())) * 1000000;

        ZonedDateTime utcZoned = timestamp.toLocalDateTime().atZone(ZoneId.of("UTC"));
        ZonedDateTime ldtZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault());

        StringBuilder date = new StringBuilder(ldtZoned.toString());
        int year = Integer.parseInt(date.substring(0, date.indexOf("-")));
        date.delete(0, date.indexOf("-") + 1);
        int month = Integer.parseInt(date.substring(0, date.indexOf("-")));
        date.delete(0, date.indexOf("-") + 1);
        int day = Integer.parseInt(date.substring(0, date.indexOf("T")));
        date.delete(0, date.indexOf("T") + 1);
        int hour = Integer.parseInt(date.substring(0, date.indexOf(":")));
        date.delete(0, date.indexOf(":") + 1);

        if (date.toString().contains("+")){
            date.delete(date.indexOf("+"), date.length());
        } else if (date.toString().contains("-")){
            date.delete(date.indexOf("-"), date.length());
        } else {
            date.delete(date.indexOf("Z"), date.length());
        }

        if(date.toString().contains(":")){
            int minute = Integer.parseInt(date.substring(0, date.indexOf(":")));
            date.delete(0, date.indexOf(":") + 1);
            int seconds = Integer.parseInt(date.substring(0, 2));
            return Timestamp.valueOf(LocalDateTime.of(year, month, day, hour, minute, seconds, milliseconds));
        } else{
            int minute = Integer.parseInt(date.substring(0, 2));
            return Timestamp.valueOf(LocalDateTime.of(year, month, day, hour, minute, 0, milliseconds));
        }
    }

    public ObservableList<Customer> lookupCustomer(String customerName) {
        ObservableList<Customer> filteredCustomer = FXCollections.observableArrayList();
        String name = customerName.trim();

        for (Customer customer : allCustomers) {
            if (customer.getCustomerName().toLowerCase().contains(name.toLowerCase())) {
                filteredCustomer.add(customer);
            }
        }
        return filteredCustomer;
    }
}
