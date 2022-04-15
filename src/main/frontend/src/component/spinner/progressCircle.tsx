import './progressCircle.css';

interface ProgressCircleProps {
    percentage: number,
}

interface CSSPros {
    color: string | undefined;
    percentage?: number
}

const ProgressCircle = ({percentage}: ProgressCircleProps) => {

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

    const Text = ({color}: CSSPros) => {
        return (
            <text
                x="50%"
                y="50%"
                dominantBaseline="central"
                textAnchor="middle"
                fill={color}
                fontSize={"1.5em"}
            >
                {percentage.toFixed(0)}%
            </text>
        );
    };

    return (
        <div className="progress-container">
            <svg width={200} height={200}>
                <g transform={`rotate(-90 ${"100 100"})`}>
                    <Circle color={"rgba(255,255,255,0.93)"} percentage={0}/>
                    <Circle color={"#00ff43"} percentage={percentage}/>
                </g>
                <Text color={"#fff"}/>
            </svg>
        </div>
    );
}

export default ProgressCircle;