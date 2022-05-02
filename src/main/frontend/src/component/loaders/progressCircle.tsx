import './progressCircle.css';
import React from "react";

interface ProgressCircleProps {
    progress: number,
    total: number,
}

interface CSSPros {
    color: string | undefined;
    percentage?: number
}

const ProgressCircle = ({progress, total}: ProgressCircleProps) => {

    const Circle = ({color, percentage}: CSSPros) => {
        if (!percentage) percentage = 0;

        const r = 50;
        const circ = 2 * Math.PI * r;
        const strokePct = ((100 - percentage) * circ) / 100; // where stroke will start, e.g. from 15% to 100%.
        return (
            <circle
                r={r}
                cx={100}
                cy={100}
                fill="transparent"
                stroke={color} // remove colour as 0% sets full circumference
                strokeWidth={"1rem"}
                strokeDasharray={circ}
                strokeDashoffset={percentage ? strokePct : 0}
            />
        );
    };

    const Text = ({color, percentage}: CSSPros) => {
        return (
            <text
                x="50%"
                y="50%"
                dominantBaseline="central"
                textAnchor="middle"
                fill={color}
                fontSize={"1.5em"}
            >
                {percentage ? percentage.toFixed(0) : 0}%
            </text>
        );
    };

    const calculatePercentage = () => {
        const percentage = total !== 0 ? ((100 * progress) / total).toFixed() : 0;
        const isNegativeOrNaN = !Number.isFinite(+percentage) || percentage < 0; // we can set non-numbers to 0 here
        const isTooHigh = percentage > 100;
        return isNegativeOrNaN ? 0 : isTooHigh ? 100 : +percentage;
    };

    const percentage = calculatePercentage();

    return (
        <div className="progress-container">
            <svg width={200} height={200}>
                <g transform={`rotate(-90 ${"100 100"})`}>
                    <Circle color={"rgba(255,255,255,0.93)"} percentage={0}/>
                    <Circle color={"#00ff43"} percentage={percentage}/>
                </g>
                <Text color={"#fff"} percentage={percentage}/>
            </svg>
            <p className="progress-text">{`${progress} out of  ${total} processed`}</p>
        </div>
    );
}

export default ProgressCircle;