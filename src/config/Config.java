package config;

import response.Protocol;
import response.RentCompanyResponse;

public class Config {
    public static final int PORT = 2000;
    public static final Protocol ONE_THREAD_PROTOCOL = new RentCompanyResponse();
    public static final int POOL_NUM_THREADS = 5;

    public static final String PATH_DATABASE = "src/resources/";
    public static final String FILENAME = "database.csv";
}
