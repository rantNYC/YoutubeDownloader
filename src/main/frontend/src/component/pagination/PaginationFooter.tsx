import React, {useState} from "react";
import './PaginationFooter.css';

interface PaginationFooterProps {
    totalPages: number,
    currentPage: number,

    goToLast(): void,

    goToFirst(): void,

    goToPage(page: number): void,

    goToNext(): void,

    goToPrev(): void

    changeSize(size: number): void
}

const PaginationFooter = ({
                              totalPages,
                              currentPage,
                              goToLast,
                              goToFirst,
                              goToPage,
                              goToNext,
                              goToPrev,
                              changeSize,
                          }: PaginationFooterProps) => {

    const [selected, setSelected] = useState(10);

    const pageButtonClick = (index: number) => {
        if (currentPage === index) return;
        goToPage(index);
    }

    const arrowButtonClick = (index: number, fn: () => void) => {
        if (currentPage === index) return;
        fn();
    }

    return (
        <div className="tableFooter">
            <button className='button inactiveButton' onClick={() => arrowButtonClick(0, goToFirst)}>
                {'<<'}
            </button>
            <button className='button inactiveButton' onClick={goToPrev}>
                {'<'}
            </button>
            <button className='button inactiveButton' onClick={goToNext}>
                {'>'}
            </button>
            <button className='button inactiveButton' onClick={() => arrowButtonClick(totalPages - 1, goToLast)}>
                {'>>'}
            </button>
            <div className="footerInfo">
                <span>Page <strong>{`${currentPage + 1} of ${totalPages}`}</strong> </span>
                <span>| Go to page: <input className="footer-input" type="number" min={1} max={totalPages}
                                           onInput={(e) => {
                                               let val = parseInt(e.currentTarget.value);
                                               if (val > parseInt(e.currentTarget.max) || val < parseInt(e.currentTarget.min)) {
                                                   e.currentTarget.value = '0';
                                               } else{
                                                   pageButtonClick(val-1);
                                               }
                                           }}/></span>
                <select value={selected} onChange={e => {
                    const newVal = parseInt(e.target.value);
                    setSelected(newVal);
                    changeSize(newVal);
                }}>
                    <option value="10">Show 10</option>
                    <option value="25">Show 25</option>
                    <option value="50">Show 50</option>
                </select>
            </div>
        </div>
    );
};

export default PaginationFooter;