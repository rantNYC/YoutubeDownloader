import React, {useCallback, useEffect, useState} from "react";
import LoadingSpinner from "../../component/spinner/spinner";
import axios from "axios";
import {MEDIA_FILE_FROM_SERVER, FETCH_PAGE_DATA, ZIP_ALL_FILES, STREAM_FILE_FROM_SERVER} from "../../utils/Routes";
import FileSaver from "file-saver";
import {CollectionDownload, emptyCollectionDownload, PageInfo, PageLinks} from "../../model/IDownload";
import PaginationTable from "../../component/pagination/PaginationTable";
import './dashboard.css'
import {useAppDispatch, useAppSelector} from "../store/hooks";
import {populate, remove, selectMedia} from "../store/media/mediaReducer";
import PaginationFooter from "../../component/pagination/PaginationFooter";

const Dashboard = () => {

    const data = useAppSelector(selectMedia) as CollectionDownload;
    const dispatch = useAppDispatch();
    const [loading, setLoading] = useState(false);
    const [currentPage, setCurrentPage] = useState(data.page?.number);

    const sendRequest = useCallback((url: string, page: number) => {
        axios.get(url)
            .then(res => {
                dispatch(populate(res.data));
                setCurrentPage(page)
            }).catch(err =>
                console.error("Error", err)
            );
    }, [dispatch]);

    useEffect(() => {
        if (data === emptyCollectionDownload) {
            sendRequest(FETCH_PAGE_DATA(0, 10), 0);
        }
    }, [data, sendRequest])

    let pageInfo: PageInfo | undefined;
    if (data.page == null) {
        pageInfo = undefined
    } else {
        pageInfo = data.page;
    }

    const loadTable = () => {
        return (
            <>
                <PaginationTable data={data._embedded.youtubeDataInfoList} downloadFile={downloadFile}
                                 deleteFile={deleteFile}/>
                {!pageInfo ?
                    <></> :
                    <PaginationFooter
                        currentPage={currentPage as number}
                        totalPages={pageInfo.totalPages}
                        goToFirst={goToFirst}
                        goToLast={goToLast}
                        goToPage={goToPage}
                        goToNext={goToNext}
                        goToPrev={goToPrev}/>}
            </>
        )
    }

    const goToPage = (page: number) => {
        if (!pageInfo) {
            return;
        } else if (page > pageInfo.totalPages) {
            goToLast()
        } else if (page < 0) {
            goToFirst();
        }
        sendRequest(FETCH_PAGE_DATA(page, pageInfo.size), page);
    }

    const goToLast = () => {
        if (!pageInfo) {
            return;
        }
        sendRequest((data._links as PageLinks).last.href, pageInfo.totalPages - 1);
    }

    const goToFirst = () => {
        if (!pageInfo) {
            return;
        }
        sendRequest((data._links as PageLinks).first.href, 0);
    }

    const goToNext = () => {
        if (!pageInfo) {
            return;
        }
        const links = data._links as PageLinks;
        if (!links.next) return;
        sendRequest(links.next.href, pageInfo.number + 1);
    }

    const goToPrev = () => {
        if (!pageInfo) {
            return;
        }
        const links = data._links as PageLinks;
        if (!links.prev) return;
        sendRequest(links.prev.href, pageInfo.number - 1);
    }

    const downloadFile = (id: number, fileWithExtension: string) => {
        axios.get(STREAM_FILE_FROM_SERVER(id), {
            responseType: "blob",
        }).then(response => {
            return new Blob([response.data]);
        }).then(blob => {
            FileSaver.saveAs(blob, fileWithExtension);
        }).catch((response) => {
            console.error("Could not download from backend.", response);
        })
    }

    const downloadAllFiles = () => {
        setLoading(true);
        const ids: string[] = []
        data._embedded.youtubeDataInfoList.forEach(data => ids.push(data.id.toString()));
        axios.post(ZIP_ALL_FILES, {ids: ids}, {
            responseType: "blob",
        }).then(response => {
            return new Blob([response.data]);
        }).then(blob => {
            FileSaver.saveAs(blob, "music.zip");
        }).catch((response) => {
            console.error("Could not download from backend.", response);
        }).finally(() => {
            setLoading(false);
        });
    }

    const deleteFile = (id: number) => {
        axios.delete(MEDIA_FILE_FROM_SERVER(id))
            .catch((response) => {
                console.error("Could not download from backend.", response);
            })
        dispatch(remove(id));
    }

    return (
        <>
            <button className="btn" disabled={loading} onClick={downloadAllFiles}>Download All</button>
            {loading ? <LoadingSpinner/> : loadTable()}
        </>
    )
}

export default Dashboard;