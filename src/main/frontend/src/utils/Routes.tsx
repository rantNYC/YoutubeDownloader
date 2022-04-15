export const BASE_URL = "/api"
export const DOWNLOAD_URL = BASE_URL + "/download"
export const PROGRESS_URL = DOWNLOAD_URL + "/progress"
export const MEDIA_URL = DOWNLOAD_URL + "/media"
export const FETCH_ALL_FILES = DOWNLOAD_URL + "/data/all"
export const FETCH_FILE_USER = DOWNLOAD_URL + "/get/user";
export const ZIP_ALL_FILES = DOWNLOAD_URL + "/file/zip";
export const STREAM_FILE_FROM_SERVER = (id: number) => MEDIA_URL + `/stream/${id}`;
export const MEDIA_FILE_FROM_SERVER = (id: number) => MEDIA_URL + `/${id}`;
export const FETCH_PAGE_DATA = (page: number, size: number) => `${MEDIA_URL}?page=${page}&size=${size}`

export const handleError = (error: any) => {
    if (error.response) {
        console.error(error.response.data);
    } else if (error.request) {
        console.log(error.request);
    } else {
        console.log('Error', error.message);
    }
}