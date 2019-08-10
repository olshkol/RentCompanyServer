package response;

import cars.Request;
import cars.Response;

public interface Protocol {
    Response getResponse(Request request);
}
