import React from "react";
import './PaginationFooter.css';

interface PaginationFooterProps {
    totalPages: number,
    currentPage: number,

    goToLast(): void,

    goToFirst(): void,

    goToPage(page: number): void,

    goToNext(): void,

    goToPrev(): void
}

const PaginationFooter = ({
                              totalPages,
                              currentPage,
                              goToLast,
                              goToFirst,
                              goToPage,
                              goToNext,
                              goToPrev,
                          }: PaginationFooterProps) => {

    const range = Array.from({length: totalPages}, (_, i) => i + 1)

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
            {range.map((el, index) => (
                <button
                    key={index}
                    className={`button ${
                        currentPage === index ? "activeButton" : "inactiveButton"
                    }`}
                    onClick={() => pageButtonClick(index)}
                >
                    {el}
                </button>
            ))}
            <button className='button inactiveButton' onClick={goToNext}>
                {'>'}
            </button>
            <button className='button inactiveButton' onClick={() => arrowButtonClick(totalPages-1, goToLast)}>
                {'>>'}
            </button>
        </div>
    );
};

export default PaginationFooter;