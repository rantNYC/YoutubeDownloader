import './progressBar.css'

interface ProgressBarProps{
    progress: number,
    title: string,
}

const ProgressBar = ({progress, title} : ProgressBarProps) => {
    return (
        <div className="progress">
            <div className="progress-value" style={{width: `${progress}`}}></div>
            <label className="progress-text">{title}</label>
        </div>
    )
}

export default ProgressBar;