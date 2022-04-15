import {IDownload} from "../../model/IDownload";
import './PaginationTable.css';

interface PaginationTableProps {
    data: IDownload[],

    downloadFile(id: number, fileName: string) : void;
    deleteFile(id: number) : void;
}

const PaginationTable = ({data, downloadFile, deleteFile}: PaginationTableProps) => {

    return (
        <>
            <div className='table-container'>
                <table className='fl-table'>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Title</th>
                        <th>Download</th>
                        <th>Remove</th>
                    </tr>
                    </thead>
                    <tbody>
                    {data.map((item) => (
                        <tr key={item.id}>
                            <td className="fl-id">{item.id}</td>
                            <td><a href={item.urlId} rel="noreferrer nofollow" target="_blank">{item.title}</a></td>
                            <td className="fl-icon">
                                  <i className="fa fa-download" onClick={() => downloadFile(item.id, item.fileWithExtension)}/>
                            </td>
                            <td className="fl-icon">
                                <i className="fas fa-trash-alt" onClick={() => deleteFile(item.id)}/>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </>
    )

}

export default PaginationTable;
