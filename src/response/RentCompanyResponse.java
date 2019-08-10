package response;

import cars.Package;
import cars.Request;
import cars.Response;
import cars.StatusCode;
import main.cars.model.RentCompanyImpl;
import main.cars.model.annotations.Routing;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

import static config.Config.*;
import static response.Utils.getClassesForPackage;

public class RentCompanyResponse implements Protocol {
    private static RentCompanyImpl rentCompany;

    static {
        try {
            rentCompany = (RentCompanyImpl) RentCompanyImpl.restoreFromFile(PATH_DATABASE + FILENAME);
        } catch (Exception e) {
            rentCompany = new RentCompanyImpl();
        }
    }

    private static Package getParamsInfo(Request request) {
        Routing annotation = null;
        Package newPackage = null;
        for (Method method : rentCompany.getClass().getDeclaredMethods()) {
            annotation = method.getAnnotation(Routing.class);
            if (annotation != null && annotation.value().equals(request.getQuery())) {
                Parameter[] parameters = method.getParameters();
                newPackage = new Package();
                for (Parameter parameter : parameters) {
                    newPackage.addParamClasses(parameter.getType());
                    if (!getClassesForPackage(PATH_DTO_CLASSES).contains(parameter.getType()))
                        newPackage.addCompositeParamsForMethods(parameter);
                    else newPackage.addCompositeParamsForConstructors(parameter);
                }
                break;
            }
        }
        return newPackage;
    }

    public static Object[] getRealParametres(Request request){
        Object[] realParams = new Object[request.getPackage().paramsClassesSize()];
        for (int i = 0; i < request.getPackage().paramsClassesSize(); i++) {
            if (getClassesForPackage(PATH_DTO_CLASSES).contains(request.getPackage().getParamsClasses().get(i))) {
                ArrayList<Class> classes = request.getPackage().getCompositeParamsClasses().get(i);
                try {
                    Constructor declaredConstructor = request.getPackage().getParamsClasses().get(i).getDeclaredConstructor(classes.toArray(new Class[0]));
                    Object p = declaredConstructor.newInstance(request.getPackage().getCompositeParams().get(i).toArray(new Object[0]));
                    realParams[i] = p;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else realParams[i] = request.getPackage().getCompositeParams().get(i).get(0);
        }
        return realParams;
    }


    @Override
    public Response getResponse(Request request) {
        if (request.getPackage() == null)
            return new Response(getParamsInfo(request));

        Object[] realParams =  getRealParametres(request);

        Response response = null;
        Routing annotation = null;
        for (Method method : rentCompany.getClass().getDeclaredMethods()) {
            annotation = method.getAnnotation(Routing.class);
            if (annotation != null && annotation.value().equals(request.getQuery())) {
                try {
                    response = new Response(
                            (Serializable) method.invoke(rentCompany, realParams)
                            , StatusCode.OK);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                if (request.getQuery().contains("Add") || request.getQuery().contains("Remove") ||
                        request.getQuery().contains("Rent") || request.getQuery().contains("Return"))
                    rentCompany.save(PATH_DATABASE + FILENAME);
                break;
            }
        }
        return response;
    }
}
