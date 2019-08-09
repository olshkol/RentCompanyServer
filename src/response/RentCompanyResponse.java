package response;

import cars.dto.Request;
import cars.dto.Response;
import cars.dto.TCPResponse;
import cars.model.RequestParamCustom;
import main.cars.model.RentCompanyImpl;
import main.cars.model.annotations.Routing;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static config.Config.FILENAME;
import static config.Config.PATH_DATABASE;

public class RentCompanyResponse implements Protocol {
    private static RentCompanyImpl rentCompany;
    private static Response response;

    static {
        try {
            rentCompany = (RentCompanyImpl) RentCompanyImpl.restoreFromFile(PATH_DATABASE + FILENAME);
        } catch (Exception e) {
            rentCompany = new RentCompanyImpl();
        }
    }

    public static List<Class> getClassesForPackage(){
        String pathname = "C:\\Users\\Oleg\\Desktop\\Tel-Ran\\2019-07-31_Project\\DTO\\src\\cars\\model\\";
        File file = new File(pathname);

        Class[] classes = Arrays.stream(file.list((dir, name) -> name.endsWith(".java")))
                .map(s -> s.replaceAll(".java", ""))
                .map((String className) -> {
                    try {
                        return Class.forName("cars.model." + className);
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(Class[]::new);
        return Arrays.asList(classes);
    }


    public Response gettypeAndCountParams(Request request){

        Routing annotation = null;
        int countParam = 0;
        ArrayList<Class> paramsClasses = new ArrayList<>();
        ArrayList<ArrayList<Class>> compositeParamsClasses = new ArrayList<>();
        ArrayList<ArrayList<String>> compositeParamsNames = new ArrayList<>();

        Response response  = null;
        for (Method method : rentCompany.getClass().getDeclaredMethods()) {
            annotation = method.getAnnotation(Routing.class);
            if (annotation != null && annotation.value().equals(request.getQuery())) {

                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    if (!getClassesForPackage().contains(parameters[i].getType())) {
                        paramsClasses.add(parameters[i].getType());

                        compositeParamsClasses.add(new ArrayList<>());
                        compositeParamsClasses.get(i).add(parameters[i].getType());
                        compositeParamsNames.add(new ArrayList<>());
                        compositeParamsNames.get(i).add(parameters[i].getAnnotation(RequestParamCustom.class).value()); // name param

                        countParam++;
                    }
                    else {
                        paramsClasses.add(parameters[i].getType());
                        compositeParamsClasses.add(new ArrayList<>());
                        compositeParamsNames.add(new ArrayList<>());

                        Constructor<?>[] declaredConstructors = parameters[i].getType().getDeclaredConstructors();
                        for (int j = 0; j < declaredConstructors.length; j++) {
                            if (declaredConstructors[j].getParameterCount() != 0) {
                                Collections.addAll(compositeParamsClasses.get(i), declaredConstructors[j].getParameterTypes());

                                Parameter[] parametersConstruct = declaredConstructors[j].getParameters();
                                for (int k = 0; k < declaredConstructors[j].getParameterCount(); k++) {
                                    compositeParamsNames.get(i).add(parametersConstruct[k].getAnnotation(RequestParamCustom.class).value());
                                }
                                countParam++;
                                break;
                            }
                        }
                    }
                }
                break;
            }
        }
        response = new Response(countParam, paramsClasses, compositeParamsClasses, compositeParamsNames);
        RentCompanyResponse.response = response;
        return response;
    }


    @Override
    public Response getResponse(Request request) {
        Response response = null;

        if  (request.getBody()==null)
            return gettypeAndCountParams(request);

        //for not exception EOF
        request.setCompositeParamsClasses(RentCompanyResponse.response.getCompositeParamsClasses());
        request.setParamsClasses(RentCompanyResponse.response.getParamsClasses());


        //make real parameters
        int size = request.getParamsClasses().size();
        Object[] realParams = new Object[size];
        for (int i = 0; i < size; i++) {
            if (getClassesForPackage().contains(request.getParamsClasses().get(i))){
                ArrayList<Class> classes = request.getCompositeParamsClasses().get(i);
                try {
                    Constructor declaredConstructor = request.getParamsClasses().get(i).getDeclaredConstructor(classes.toArray(new Class[0]));
                    Object p = declaredConstructor.newInstance(request.getCompositeParams().get(i).toArray(new Object[0]));
                    realParams[i] = p;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }

            }
            else realParams[i] = request.getCompositeParams().get(i).get(0);
        }
        //that's all


        Routing annotation = null;
        for (Method method : rentCompany.getClass().getDeclaredMethods()) {
            annotation = method.getAnnotation(Routing.class);
            if (annotation != null && annotation.value().equals(request.getQuery())) {
                try {
                    //not request body
                        response = new Response(
                                (Serializable) method.invoke(rentCompany, realParams)
                                , TCPResponse.OK);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                if (request.getQuery().contains("add") || request.getQuery().contains("remove") ||
                    request.getQuery().contains("rent") || request.getQuery().contains("return"))
                    rentCompany.save(PATH_DATABASE + FILENAME);
                break;
            }
        }
        return response;
    }
}
