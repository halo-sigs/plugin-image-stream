/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.19.3
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


import type { Configuration } from '../configuration';
import type { AxiosPromise, AxiosInstance, RawAxiosRequestConfig } from 'axios';
import globalAxios from 'axios';
// Some imports not used depending on template conditions
// @ts-ignore
import { DUMMY_BASE_URL, assertParamExists, setApiKeyToObject, setBasicAuthToObject, setBearerAuthToObject, setOAuthToObject, setSearchParams, serializeDataIfNeeded, toPathString, createRequestFunction } from '../common';
// @ts-ignore
import { BASE_PATH, COLLECTION_FORMATS, type RequestArgs, BaseAPI, RequiredError, operationServerMap } from '../base';
/**
 * PixabayV1alpha1Api - axios parameter creator
 * @export
 */
export const PixabayV1alpha1ApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Search images
         * @param {string} [q] A URL encoded search term. If omitted, all images are returned. This value may not exceed 100 characters. Example: \&quot;yellow+flower\&quot;
         * @param {number} [page] Returned search results are paginated. Use this parameter to select the page number. Default: 1
         * @param {number} [perPage] Determine the number of results per page. Accepted values: 3 - 200 Default: 20 
         * @param {string} [imageType]  Filter results by image type.  Accepted values: \&quot;all\&quot;, \&quot;photo\&quot;, \&quot;illustration\&quot;, \&quot;vector\&quot;  Default: \&quot;all\&quot; 
         * @param {string} [category] Filter results by category. Accepted values: backgrounds, fashion, nature, science, education, feelings, health, people, religion, places, animals, industry, computer, food, sports, transportation, travel,  buildings, business, music 
         * @param {string} [order] How the results should be ordered. Accepted values: \&quot;popular\&quot;, \&quot;latest\&quot; Default: \&quot;popular\&quot; 
         * @param {string} [safesearch] A flag indicating that only images suitable for all ages should be returned. Accepted values: \&quot;true\&quot;, \&quot;false\&quot; Default: \&quot;false\&quot; 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        searchPixImages: async (q?: string, page?: number, perPage?: number, imageType?: string, category?: string, order?: string, safesearch?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/pixabay.halo.run/v1alpha1/photos/-/search`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'GET', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication basicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication bearerAuth required
            // http bearer authentication required
            await setBearerAuthToObject(localVarHeaderParameter, configuration)

            if (q !== undefined) {
                localVarQueryParameter['q'] = q;
            }

            if (page !== undefined) {
                localVarQueryParameter['page'] = page;
            }

            if (perPage !== undefined) {
                localVarQueryParameter['per_page'] = perPage;
            }

            if (imageType !== undefined) {
                localVarQueryParameter['image_type'] = imageType;
            }

            if (category !== undefined) {
                localVarQueryParameter['category'] = category;
            }

            if (order !== undefined) {
                localVarQueryParameter['order'] = order;
            }

            if (safesearch !== undefined) {
                localVarQueryParameter['safesearch'] = safesearch;
            }


    
            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * PixabayV1alpha1Api - functional programming interface
 * @export
 */
export const PixabayV1alpha1ApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = PixabayV1alpha1ApiAxiosParamCreator(configuration)
    return {
        /**
         * Search images
         * @param {string} [q] A URL encoded search term. If omitted, all images are returned. This value may not exceed 100 characters. Example: \&quot;yellow+flower\&quot;
         * @param {number} [page] Returned search results are paginated. Use this parameter to select the page number. Default: 1
         * @param {number} [perPage] Determine the number of results per page. Accepted values: 3 - 200 Default: 20 
         * @param {string} [imageType]  Filter results by image type.  Accepted values: \&quot;all\&quot;, \&quot;photo\&quot;, \&quot;illustration\&quot;, \&quot;vector\&quot;  Default: \&quot;all\&quot; 
         * @param {string} [category] Filter results by category. Accepted values: backgrounds, fashion, nature, science, education, feelings, health, people, religion, places, animals, industry, computer, food, sports, transportation, travel,  buildings, business, music 
         * @param {string} [order] How the results should be ordered. Accepted values: \&quot;popular\&quot;, \&quot;latest\&quot; Default: \&quot;popular\&quot; 
         * @param {string} [safesearch] A flag indicating that only images suitable for all ages should be returned. Accepted values: \&quot;true\&quot;, \&quot;false\&quot; Default: \&quot;false\&quot; 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async searchPixImages(q?: string, page?: number, perPage?: number, imageType?: string, category?: string, order?: string, safesearch?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<object>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.searchPixImages(q, page, perPage, imageType, category, order, safesearch, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PixabayV1alpha1Api.searchPixImages']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * PixabayV1alpha1Api - factory interface
 * @export
 */
export const PixabayV1alpha1ApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = PixabayV1alpha1ApiFp(configuration)
    return {
        /**
         * Search images
         * @param {PixabayV1alpha1ApiSearchPixImagesRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        searchPixImages(requestParameters: PixabayV1alpha1ApiSearchPixImagesRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<object> {
            return localVarFp.searchPixImages(requestParameters.q, requestParameters.page, requestParameters.perPage, requestParameters.imageType, requestParameters.category, requestParameters.order, requestParameters.safesearch, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for searchPixImages operation in PixabayV1alpha1Api.
 * @export
 * @interface PixabayV1alpha1ApiSearchPixImagesRequest
 */
export interface PixabayV1alpha1ApiSearchPixImagesRequest {
    /**
     * A URL encoded search term. If omitted, all images are returned. This value may not exceed 100 characters. Example: \&quot;yellow+flower\&quot;
     * @type {string}
     * @memberof PixabayV1alpha1ApiSearchPixImages
     */
    readonly q?: string

    /**
     * Returned search results are paginated. Use this parameter to select the page number. Default: 1
     * @type {number}
     * @memberof PixabayV1alpha1ApiSearchPixImages
     */
    readonly page?: number

    /**
     * Determine the number of results per page. Accepted values: 3 - 200 Default: 20 
     * @type {number}
     * @memberof PixabayV1alpha1ApiSearchPixImages
     */
    readonly perPage?: number

    /**
     *  Filter results by image type.  Accepted values: \&quot;all\&quot;, \&quot;photo\&quot;, \&quot;illustration\&quot;, \&quot;vector\&quot;  Default: \&quot;all\&quot; 
     * @type {string}
     * @memberof PixabayV1alpha1ApiSearchPixImages
     */
    readonly imageType?: string

    /**
     * Filter results by category. Accepted values: backgrounds, fashion, nature, science, education, feelings, health, people, religion, places, animals, industry, computer, food, sports, transportation, travel,  buildings, business, music 
     * @type {string}
     * @memberof PixabayV1alpha1ApiSearchPixImages
     */
    readonly category?: string

    /**
     * How the results should be ordered. Accepted values: \&quot;popular\&quot;, \&quot;latest\&quot; Default: \&quot;popular\&quot; 
     * @type {string}
     * @memberof PixabayV1alpha1ApiSearchPixImages
     */
    readonly order?: string

    /**
     * A flag indicating that only images suitable for all ages should be returned. Accepted values: \&quot;true\&quot;, \&quot;false\&quot; Default: \&quot;false\&quot; 
     * @type {string}
     * @memberof PixabayV1alpha1ApiSearchPixImages
     */
    readonly safesearch?: string
}

/**
 * PixabayV1alpha1Api - object-oriented interface
 * @export
 * @class PixabayV1alpha1Api
 * @extends {BaseAPI}
 */
export class PixabayV1alpha1Api extends BaseAPI {
    /**
     * Search images
     * @param {PixabayV1alpha1ApiSearchPixImagesRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PixabayV1alpha1Api
     */
    public searchPixImages(requestParameters: PixabayV1alpha1ApiSearchPixImagesRequest = {}, options?: RawAxiosRequestConfig) {
        return PixabayV1alpha1ApiFp(this.configuration).searchPixImages(requestParameters.q, requestParameters.page, requestParameters.perPage, requestParameters.imageType, requestParameters.category, requestParameters.order, requestParameters.safesearch, options).then((request) => request(this.axios, this.basePath));
    }
}

