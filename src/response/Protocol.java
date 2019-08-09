package response;

import cars.dto.Request;
import cars.dto.Response;

public interface Protocol {
    Response getResponse(Request request);
}
