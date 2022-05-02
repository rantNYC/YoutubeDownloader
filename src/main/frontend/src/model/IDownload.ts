export interface CollectionDownload {
    _embedded: Embedded;
    _links: ReferenceLinks | PageLinks;
    page?: PageInfo;
}

export interface Embedded {
    youtubeDataInfoList: IDownload[];
}

export interface IDownload {
    id: number;
    title: string;
    urlId: string;
    ext: string;
    size: number;
    lengthSeconds: number;
    fileWithExtension: string;
    video: boolean;
    _links: ReferenceLinks;
}

export interface ReferenceLinks extends LinksCommon {
    all?: HRef;
}

export interface HRef {
    href: string;
}

export interface PageInfo {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
}

export interface PageLinks extends LinksCommon {
    first: HRef;
    prev?: HRef;
    next?: HRef;
    last: HRef;
}

export interface LinksCommon {
    self: HRef;
}

export const emptyHRef: HRef = {href: ''};
export const emptyPageLinks: PageLinks = {first: emptyHRef, last: emptyHRef, self: emptyHRef};
export const emptyPageInfo: PageInfo = {number: 0, size: 0, totalElements: 0, totalPages: 0};
export const emptyLinks: ReferenceLinks = {self: emptyHRef};
export const emptyIDonwload: IDownload = {
    _links: emptyLinks,
    ext: "",
    fileWithExtension: "",
    id: 0,
    lengthSeconds: 0,
    size: 0,
    title: "",
    urlId: "",
    video: false
};
export const emptyEmbedded: Embedded = {youtubeDataInfoList: []};
export const emptyCollectionDownload: CollectionDownload = {_embedded: emptyEmbedded, _links: emptyLinks};
