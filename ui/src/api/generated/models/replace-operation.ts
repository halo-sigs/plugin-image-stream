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



/**
 * 
 * @export
 * @interface ReplaceOperation
 */
export interface ReplaceOperation {
    /**
     * 
     * @type {string}
     * @memberof ReplaceOperation
     */
    'op': ReplaceOperationOpEnum;
    /**
     * A JSON Pointer path pointing to the location to move/copy from.
     * @type {string}
     * @memberof ReplaceOperation
     */
    'path': string;
    /**
     * Value can be any JSON value
     * @type {any}
     * @memberof ReplaceOperation
     */
    'value': any;
}

export const ReplaceOperationOpEnum = {
    Replace: 'replace'
} as const;

export type ReplaceOperationOpEnum = typeof ReplaceOperationOpEnum[keyof typeof ReplaceOperationOpEnum];

