package io.metersphere.api.parse;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.Scenario;
import io.metersphere.api.dto.scenario.request.HttpRequest;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.service.ApiModuleService;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class ApiImportAbstractParser implements ApiImportParser {

    protected String projectId;
    protected ApiModuleService apiModuleService;

    protected String getApiTestStr(InputStream source) {
        StringBuilder testStr = null;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source, StandardCharsets.UTF_8))) {
            testStr = new StringBuilder();
            String inputStr;
            while ((inputStr = bufferedReader.readLine()) != null) {
                testStr.append(inputStr);
            }
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
            LogUtil.error(e.getMessage(), e);
        } finally {
            try {
                source.close();
            } catch (IOException e) {
                MSException.throwException(e.getMessage());
                LogUtil.error(e.getMessage(), e);
            }
        }
        return testStr.toString();
    }

    protected void setScenarioByRequest(Scenario scenario, ApiTestImportRequest request) {
        if (request.getUseEnvironment()) {
            scenario.setEnvironmentId(request.getEnvironmentId());
        }
    }

    protected void createModule(ApiModule module) {
        module.setProtocol(RequestType.HTTP);
        List<ApiModule> apiModules = apiModuleService.selectSameModule(module);
        if (CollectionUtils.isEmpty(apiModules)) {
            apiModuleService.addNode(module);
        } else {
            module.setId(apiModules.get(0).getId());
        }
    }

    protected ApiDefinitionResult buildApiDefinition(String id, String name, String path, String method) {
        ApiDefinitionResult apiDefinition = new ApiDefinitionResult();
        apiDefinition.setName(name);
        apiDefinition.setPath(path);
        apiDefinition.setProtocol(RequestType.HTTP);
        apiDefinition.setMethod(method);
        apiDefinition.setId(id);
        apiDefinition.setProjectId(this.projectId);
        apiDefinition.setUserId(SessionUtils.getUserId());
        return apiDefinition;
    }

    protected MsHTTPSamplerProxy buildRequest(String name, String path, String method) {
        MsHTTPSamplerProxy request = new MsHTTPSamplerProxy();
        request.setName(name);
        request.setPath(path);
        request.setMethod(method);
        request.setProtocol(RequestType.HTTP);
        request.setId(UUID.randomUUID().toString());
        request.setHeaders(new ArrayList<>());
        request.setArguments(new ArrayList<>());
        request.setRest(new ArrayList<>());
        request.setBody(new Body());
        return request;
    }

    protected void addContentType(HttpRequest request, String contentType) {
//        addHeader(request, "Content-Type", contentType);
    }

    protected void addCookie(List<KeyValue> headers, String key, String value) {
        addCookie(headers, key, value, "");
    }

    protected void addCookie(List<KeyValue> headers, String key, String value, String description) {
        boolean hasCookie = false;
        for (KeyValue header : headers) {
            if (StringUtils.equalsIgnoreCase("Cookie", header.getName())) {
                hasCookie = true;
                String cookies = Optional.ofNullable(header.getValue()).orElse("");
                header.setValue(cookies + key + "=" + value + ";");
            }
        }
        if (!hasCookie) {
            addHeader(headers, "Cookie", key + "=" + value + ";", description);
        }
    }

    protected void addHeader(List<KeyValue> headers, String key, String value) {
        addHeader(headers, key, value, "");
    }

    protected void addHeader(List<KeyValue> headers, String key, String value, String description) {
        boolean hasContentType = false;
        for (KeyValue header : headers) {
            if (StringUtils.equalsIgnoreCase(header.getName(), key)) {
                hasContentType = true;
            }
        }
        if (!hasContentType) {
            headers.add(new KeyValue(key, value, description));
        }
    }
//    protected void addHeader(HttpRequest request, String key, String value) {
//        List<KeyValue> headers = Optional.ofNullable(request.getHeaders()).orElse(new ArrayList<>());
//        boolean hasContentType = false;
//        for (KeyValue header : headers) {
//            if (StringUtils.equalsIgnoreCase(header.getName(), key)) {
//                hasContentType = true;
//            }
//        }
//        if (!hasContentType) {
//            headers.add(new KeyValue(key, value));
//        }
//        request.setHeaders(headers);
//    }
}
