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
 * UnsplashV1alpha1Api - axios parameter creator
 * @export
 */
export const UnsplashV1alpha1ApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Retrieve a topic’s photos.
         * @param {string} idOrSlug The topic ID or slug.
         * @param {number} [page] Page number to retrieve. (Optional; default: 1)
         * @param {number} [perPage] Number of items per page. (Optional; default: 10)
         * @param {string} [orderBy] How to sort the photos. (Optional; default: latest). Valid values are latest and oldest.
         * @param {string} [orientation] Filter by photo orientation. (Optional; Valid values: landscape, portrait, squarish)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getUnsplashTopicPhotos: async (idOrSlug: string, page?: number, perPage?: number, orderBy?: string, orientation?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'idOrSlug' is not null or undefined
            assertParamExists('getUnsplashTopicPhotos', 'idOrSlug', idOrSlug)
            const localVarPath = `/apis/unsplash.halo.run/v1alpha1/topics/{idOrSlug}/photos`
                .replace(`{${"idOrSlug"}}`, encodeURIComponent(String(idOrSlug)));
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

            if (page !== undefined) {
                localVarQueryParameter['page'] = page;
            }

            if (perPage !== undefined) {
                localVarQueryParameter['per_page'] = perPage;
            }

            if (orderBy !== undefined) {
                localVarQueryParameter['order_by'] = orderBy;
            }

            if (orientation !== undefined) {
                localVarQueryParameter['orientation'] = orientation;
            }


    
            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Get a single page from the list of all topics.
         * @param {number} [page] Page number to retrieve. (Optional; default: 1)
         * @param {number} [perPage] Number of items per page. (Optional; default: 10)
         * @param {string} [ids] Limit to only matching topic ids or slugs. (Optional; Comma separated string)
         * @param {string} [orderBy] How to sort the topics. (Optional; Valid values: featured, latest, oldest, position; default: position)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listUnsplashTopics: async (page?: number, perPage?: number, ids?: string, orderBy?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/unsplash.halo.run/v1alpha1/topics`;
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

            if (page !== undefined) {
                localVarQueryParameter['page'] = page;
            }

            if (perPage !== undefined) {
                localVarQueryParameter['per_page'] = perPage;
            }

            if (ids !== undefined) {
                localVarQueryParameter['ids'] = ids;
            }

            if (orderBy !== undefined) {
                localVarQueryParameter['order_by'] = orderBy;
            }


    
            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Search photos
         * @param {string} query Search terms
         * @param {number} [page] Page number to retrieve. (Optional; default: 1)
         * @param {number} [perPage] Number of items per page. (Optional; default: 10)
         * @param {string} [orderBy] How to sort the photos. (Optional; default: relevant). Valid values are latest and relevant.
         * @param {string} [color] Filter results by color. Optional. Valid values are: black_and_white, black, white, yellow, orange, red, purple, magenta, green, teal, and blue.
         * @param {string} [orientation] Filter by photo orientation. Optional. (Valid values: landscape, portrait, squarish)
         * @param {string} [contentFilter] Limit results by content safety. (Optional; default: low). Valid values are low and high.
         * @param {string} [collections] Collection ID(‘s) to narrow search. Optional. If multiple, comma-separated.
         * @param {string} [lang] Beta parameters: Supported ISO 639-1 language code of the query. Optional, default: \&quot;en\&quot;
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        searchUnsplashPhotos: async (query: string, page?: number, perPage?: number, orderBy?: string, color?: string, orientation?: string, contentFilter?: string, collections?: string, lang?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'query' is not null or undefined
            assertParamExists('searchUnsplashPhotos', 'query', query)
            const localVarPath = `/apis/unsplash.halo.run/v1alpha1/photos/-/search`;
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

            if (query !== undefined) {
                localVarQueryParameter['query'] = query;
            }

            if (page !== undefined) {
                localVarQueryParameter['page'] = page;
            }

            if (perPage !== undefined) {
                localVarQueryParameter['per_page'] = perPage;
            }

            if (orderBy !== undefined) {
                localVarQueryParameter['order_by'] = orderBy;
            }

            if (color !== undefined) {
                localVarQueryParameter['color'] = color;
            }

            if (orientation !== undefined) {
                localVarQueryParameter['orientation'] = orientation;
            }

            if (contentFilter !== undefined) {
                localVarQueryParameter['content_filter'] = contentFilter;
            }

            if (collections !== undefined) {
                localVarQueryParameter['collections'] = collections;
            }

            if (lang !== undefined) {
                localVarQueryParameter['lang'] = lang;
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
 * UnsplashV1alpha1Api - functional programming interface
 * @export
 */
export const UnsplashV1alpha1ApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = UnsplashV1alpha1ApiAxiosParamCreator(configuration)
    return {
        /**
         * Retrieve a topic’s photos.
         * @param {string} idOrSlug The topic ID or slug.
         * @param {number} [page] Page number to retrieve. (Optional; default: 1)
         * @param {number} [perPage] Number of items per page. (Optional; default: 10)
         * @param {string} [orderBy] How to sort the photos. (Optional; default: latest). Valid values are latest and oldest.
         * @param {string} [orientation] Filter by photo orientation. (Optional; Valid values: landscape, portrait, squarish)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getUnsplashTopicPhotos(idOrSlug: string, page?: number, perPage?: number, orderBy?: string, orientation?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Array<object>>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getUnsplashTopicPhotos(idOrSlug, page, perPage, orderBy, orientation, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['UnsplashV1alpha1Api.getUnsplashTopicPhotos']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Get a single page from the list of all topics.
         * @param {number} [page] Page number to retrieve. (Optional; default: 1)
         * @param {number} [perPage] Number of items per page. (Optional; default: 10)
         * @param {string} [ids] Limit to only matching topic ids or slugs. (Optional; Comma separated string)
         * @param {string} [orderBy] How to sort the topics. (Optional; Valid values: featured, latest, oldest, position; default: position)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async listUnsplashTopics(page?: number, perPage?: number, ids?: string, orderBy?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<object>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.listUnsplashTopics(page, perPage, ids, orderBy, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['UnsplashV1alpha1Api.listUnsplashTopics']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Search photos
         * @param {string} query Search terms
         * @param {number} [page] Page number to retrieve. (Optional; default: 1)
         * @param {number} [perPage] Number of items per page. (Optional; default: 10)
         * @param {string} [orderBy] How to sort the photos. (Optional; default: relevant). Valid values are latest and relevant.
         * @param {string} [color] Filter results by color. Optional. Valid values are: black_and_white, black, white, yellow, orange, red, purple, magenta, green, teal, and blue.
         * @param {string} [orientation] Filter by photo orientation. Optional. (Valid values: landscape, portrait, squarish)
         * @param {string} [contentFilter] Limit results by content safety. (Optional; default: low). Valid values are low and high.
         * @param {string} [collections] Collection ID(‘s) to narrow search. Optional. If multiple, comma-separated.
         * @param {string} [lang] Beta parameters: Supported ISO 639-1 language code of the query. Optional, default: \&quot;en\&quot;
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async searchUnsplashPhotos(query: string, page?: number, perPage?: number, orderBy?: string, color?: string, orientation?: string, contentFilter?: string, collections?: string, lang?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<object>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.searchUnsplashPhotos(query, page, perPage, orderBy, color, orientation, contentFilter, collections, lang, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['UnsplashV1alpha1Api.searchUnsplashPhotos']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * UnsplashV1alpha1Api - factory interface
 * @export
 */
export const UnsplashV1alpha1ApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = UnsplashV1alpha1ApiFp(configuration)
    return {
        /**
         * Retrieve a topic’s photos.
         * @param {UnsplashV1alpha1ApiGetUnsplashTopicPhotosRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getUnsplashTopicPhotos(requestParameters: UnsplashV1alpha1ApiGetUnsplashTopicPhotosRequest, options?: RawAxiosRequestConfig): AxiosPromise<Array<object>> {
            return localVarFp.getUnsplashTopicPhotos(requestParameters.idOrSlug, requestParameters.page, requestParameters.perPage, requestParameters.orderBy, requestParameters.orientation, options).then((request) => request(axios, basePath));
        },
        /**
         * Get a single page from the list of all topics.
         * @param {UnsplashV1alpha1ApiListUnsplashTopicsRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listUnsplashTopics(requestParameters: UnsplashV1alpha1ApiListUnsplashTopicsRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<object> {
            return localVarFp.listUnsplashTopics(requestParameters.page, requestParameters.perPage, requestParameters.ids, requestParameters.orderBy, options).then((request) => request(axios, basePath));
        },
        /**
         * Search photos
         * @param {UnsplashV1alpha1ApiSearchUnsplashPhotosRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        searchUnsplashPhotos(requestParameters: UnsplashV1alpha1ApiSearchUnsplashPhotosRequest, options?: RawAxiosRequestConfig): AxiosPromise<object> {
            return localVarFp.searchUnsplashPhotos(requestParameters.query, requestParameters.page, requestParameters.perPage, requestParameters.orderBy, requestParameters.color, requestParameters.orientation, requestParameters.contentFilter, requestParameters.collections, requestParameters.lang, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for getUnsplashTopicPhotos operation in UnsplashV1alpha1Api.
 * @export
 * @interface UnsplashV1alpha1ApiGetUnsplashTopicPhotosRequest
 */
export interface UnsplashV1alpha1ApiGetUnsplashTopicPhotosRequest {
    /**
     * The topic ID or slug.
     * @type {string}
     * @memberof UnsplashV1alpha1ApiGetUnsplashTopicPhotos
     */
    readonly idOrSlug: string

    /**
     * Page number to retrieve. (Optional; default: 1)
     * @type {number}
     * @memberof UnsplashV1alpha1ApiGetUnsplashTopicPhotos
     */
    readonly page?: number

    /**
     * Number of items per page. (Optional; default: 10)
     * @type {number}
     * @memberof UnsplashV1alpha1ApiGetUnsplashTopicPhotos
     */
    readonly perPage?: number

    /**
     * How to sort the photos. (Optional; default: latest). Valid values are latest and oldest.
     * @type {string}
     * @memberof UnsplashV1alpha1ApiGetUnsplashTopicPhotos
     */
    readonly orderBy?: string

    /**
     * Filter by photo orientation. (Optional; Valid values: landscape, portrait, squarish)
     * @type {string}
     * @memberof UnsplashV1alpha1ApiGetUnsplashTopicPhotos
     */
    readonly orientation?: string
}

/**
 * Request parameters for listUnsplashTopics operation in UnsplashV1alpha1Api.
 * @export
 * @interface UnsplashV1alpha1ApiListUnsplashTopicsRequest
 */
export interface UnsplashV1alpha1ApiListUnsplashTopicsRequest {
    /**
     * Page number to retrieve. (Optional; default: 1)
     * @type {number}
     * @memberof UnsplashV1alpha1ApiListUnsplashTopics
     */
    readonly page?: number

    /**
     * Number of items per page. (Optional; default: 10)
     * @type {number}
     * @memberof UnsplashV1alpha1ApiListUnsplashTopics
     */
    readonly perPage?: number

    /**
     * Limit to only matching topic ids or slugs. (Optional; Comma separated string)
     * @type {string}
     * @memberof UnsplashV1alpha1ApiListUnsplashTopics
     */
    readonly ids?: string

    /**
     * How to sort the topics. (Optional; Valid values: featured, latest, oldest, position; default: position)
     * @type {string}
     * @memberof UnsplashV1alpha1ApiListUnsplashTopics
     */
    readonly orderBy?: string
}

/**
 * Request parameters for searchUnsplashPhotos operation in UnsplashV1alpha1Api.
 * @export
 * @interface UnsplashV1alpha1ApiSearchUnsplashPhotosRequest
 */
export interface UnsplashV1alpha1ApiSearchUnsplashPhotosRequest {
    /**
     * Search terms
     * @type {string}
     * @memberof UnsplashV1alpha1ApiSearchUnsplashPhotos
     */
    readonly query: string

    /**
     * Page number to retrieve. (Optional; default: 1)
     * @type {number}
     * @memberof UnsplashV1alpha1ApiSearchUnsplashPhotos
     */
    readonly page?: number

    /**
     * Number of items per page. (Optional; default: 10)
     * @type {number}
     * @memberof UnsplashV1alpha1ApiSearchUnsplashPhotos
     */
    readonly perPage?: number

    /**
     * How to sort the photos. (Optional; default: relevant). Valid values are latest and relevant.
     * @type {string}
     * @memberof UnsplashV1alpha1ApiSearchUnsplashPhotos
     */
    readonly orderBy?: string

    /**
     * Filter results by color. Optional. Valid values are: black_and_white, black, white, yellow, orange, red, purple, magenta, green, teal, and blue.
     * @type {string}
     * @memberof UnsplashV1alpha1ApiSearchUnsplashPhotos
     */
    readonly color?: string

    /**
     * Filter by photo orientation. Optional. (Valid values: landscape, portrait, squarish)
     * @type {string}
     * @memberof UnsplashV1alpha1ApiSearchUnsplashPhotos
     */
    readonly orientation?: string

    /**
     * Limit results by content safety. (Optional; default: low). Valid values are low and high.
     * @type {string}
     * @memberof UnsplashV1alpha1ApiSearchUnsplashPhotos
     */
    readonly contentFilter?: string

    /**
     * Collection ID(‘s) to narrow search. Optional. If multiple, comma-separated.
     * @type {string}
     * @memberof UnsplashV1alpha1ApiSearchUnsplashPhotos
     */
    readonly collections?: string

    /**
     * Beta parameters: Supported ISO 639-1 language code of the query. Optional, default: \&quot;en\&quot;
     * @type {string}
     * @memberof UnsplashV1alpha1ApiSearchUnsplashPhotos
     */
    readonly lang?: string
}

/**
 * UnsplashV1alpha1Api - object-oriented interface
 * @export
 * @class UnsplashV1alpha1Api
 * @extends {BaseAPI}
 */
export class UnsplashV1alpha1Api extends BaseAPI {
    /**
     * Retrieve a topic’s photos.
     * @param {UnsplashV1alpha1ApiGetUnsplashTopicPhotosRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UnsplashV1alpha1Api
     */
    public getUnsplashTopicPhotos(requestParameters: UnsplashV1alpha1ApiGetUnsplashTopicPhotosRequest, options?: RawAxiosRequestConfig) {
        return UnsplashV1alpha1ApiFp(this.configuration).getUnsplashTopicPhotos(requestParameters.idOrSlug, requestParameters.page, requestParameters.perPage, requestParameters.orderBy, requestParameters.orientation, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Get a single page from the list of all topics.
     * @param {UnsplashV1alpha1ApiListUnsplashTopicsRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UnsplashV1alpha1Api
     */
    public listUnsplashTopics(requestParameters: UnsplashV1alpha1ApiListUnsplashTopicsRequest = {}, options?: RawAxiosRequestConfig) {
        return UnsplashV1alpha1ApiFp(this.configuration).listUnsplashTopics(requestParameters.page, requestParameters.perPage, requestParameters.ids, requestParameters.orderBy, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Search photos
     * @param {UnsplashV1alpha1ApiSearchUnsplashPhotosRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UnsplashV1alpha1Api
     */
    public searchUnsplashPhotos(requestParameters: UnsplashV1alpha1ApiSearchUnsplashPhotosRequest, options?: RawAxiosRequestConfig) {
        return UnsplashV1alpha1ApiFp(this.configuration).searchUnsplashPhotos(requestParameters.query, requestParameters.page, requestParameters.perPage, requestParameters.orderBy, requestParameters.color, requestParameters.orientation, requestParameters.contentFilter, requestParameters.collections, requestParameters.lang, options).then((request) => request(this.axios, this.basePath));
    }
}

