import {emptyIDonwload, IDownload} from "../../model/IDownload";
import './PaginationTable.css';
import UpdateForm from "../form/updateForm";
import React, {useState} from "react";
import {STREAM_FILE_FROM_SERVER} from "../../utils/Routes";

interface PaginationTableProps {
    data: IDownload[],
    editing: boolean,

    setEditing(value: boolean): void,

    downloadFile(id: number, fileName: string): void,

    deleteFile(id: number): void,

    updateForm(dataInfo: IDownload, newTitle: string, newCategory: string): void,

}

const PaginationTable = ({
                             data,
                             editing,
                             setEditing,
                             downloadFile,
                             deleteFile,
                             updateForm,
                         }: PaginationTableProps) => {

    const [oldTitle, setOldTitle] = useState('');
    const [oldCategory, setOldCategory] = useState('');
    const [dataInfo, setDataInfo] = useState(emptyIDonwload);
    const [playing, setPlaying] = useState(false);
    const [clicked, setClicked] = useState(-1);

    const [audio, setAudio] = useState<HTMLAudioElement | undefined>(undefined);

    const playSong = (index: number) => {
        if (playing) pauseSong(index);
        const proto = new Audio(STREAM_FILE_FROM_SERVER(data[index].id));
        proto.play().then(() => setPlaying(true));
        proto.onended = () => setPlaying(false);
        setAudio(proto);
        setClicked(index);
    }

    const pauseSong = (index: number) => {
        if (audio) {
            audio.pause();
            setPlaying(false);
        }
        setClicked(-1);
    }

    const renderRow = (item: IDownload, index: number) => {
        return (
            <tr key={item.id}>
                <td suppressContentEditableWarning={true}>
                    <a href={item.urlId} rel="noreferrer nofollow"
                       target="_blank">{item.title}</a>
                </td>
                <td className="fl-id">{item.genre}</td>
                <td className="fl-icon">
                    {
                        (clicked !== index) ? <i className="fas fa-play" onClick={() => playSong(index)}/>
                            : <i className="fas fa-pause" onClick={() => pauseSong(index)}/>
                    }
                </td>
                <td className="fl-icon">
                    <button onClick={(e) => {
                        setDataInfo(item);
                        setEditing(true);
                        setOldTitle(item.title);
                        setOldCategory(item.genre);
                    }}>Edit
                    </button>
                </td>
                <td className="fl-icon">
                    <i className="fa fa-download"
                       onClick={() => downloadFile(item.id, `${item.title}.${item.ext}`)}/>
                </td>
                <td className="fl-icon">
                    <i className="fas fa-trash-alt" onClick={() => deleteFile(item.id)}/>
                </td>
            </tr>
        );
    }

    return (
        <>
            <div className='table-container'>
                <table className='fl-table'>
                    <thead>
                    <tr>
                        <th>Title</th>
                        <th>Category</th>
                        <th>Play</th>
                        <th>Edit</th>
                        <th><i className="fa fa-download"/></th>
                        <th><i className="fas fa-trash-alt"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    {data.map((item, index) => renderRow(item, index))}
                    </tbody>
                </table>
            </div>
            {editing ?
                <UpdateForm oldTitle={oldTitle} oldCategory={oldCategory} updateForm={updateForm}
                            setEditing={setEditing} data={dataInfo}/> : <></>}
        </>
    )

}

export default PaginationTable;
