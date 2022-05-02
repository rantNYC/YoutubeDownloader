import {IDownload} from "../../model/IDownload";
import './PaginationTable.css';
import {useState} from "react";

interface PaginationTableProps {
    data: IDownload[],

    downloadFile(id: number, fileName: string): void;

    deleteFile(id: number): void;

    updateTitle(id: number, title: string | null): void;
}

const PaginationTable = ({data, downloadFile, deleteFile, updateTitle}: PaginationTableProps) => {

    const [editable, setEditable] = useState(false);

    const renderRow = (item: IDownload) => {
        return (
            <tr key={item.id}>
                <td className="fl-id">{item.id}</td>
                <td suppressContentEditableWarning={true}
                    contentEditable={editable}>
                    <a href={item.urlId} rel="noreferrer nofollow"
                       target="_blank">{item.title}</a></td>
                <td className="fl-icon">
                    {!editable ? <button onClick={() => setEditable(!editable)}>Edit</button>
                        : <div>
                            <button onClick={() => setEditable(!editable)}>Save</button>
                            <button onClick={() => setEditable(!editable)}>Cancel</button>
                          </div>}
                    </td>
                <td className="fl-icon">
                    <i className="fa fa-download"
                       onClick={() => downloadFile(item.id, item.fileWithExtension)}/>
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
                        <th>ID</th>
                        <th>Title</th>
                        <th>Options</th>
                        <th>Download</th>
                        <th>Remove</th>
                    </tr>
                    </thead>
                    <tbody>
                    {data.map(item => renderRow(item))}
                    </tbody>
                </table>
            </div>
        </>
    )

}

export default PaginationTable;
