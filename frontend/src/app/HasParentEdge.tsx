import { Handle, Position, getSmoothStepPath, BaseEdge, EdgeLabelRenderer, MarkerType, type Edge, type EdgeProps } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { useState} from 'react';
export default function HasParentEdge({ id,
  sourceX,
  sourceY,
  targetX,
  targetY,
 }: EdgeProps<Edge>){
  //const [displayNumber, setDisplayNumber] = useState(data.number);
    const [edgePath, labelX, labelY] = getSmoothStepPath({sourceX, sourceY, targetX, targetY});
   return <>
    <BaseEdge id={id} path={edgePath} style={{ stroke: 'red' }} markerEnd={MarkerType.ArrowClosed}/>
    <EdgeLabelRenderer>
            <div
            style={{
                position: 'absolute',
                transform: `translate(-50%, -50%) translate(${labelX}px, ${labelY}px)`,
            }}
            >
            Parent
            </div>
        </EdgeLabelRenderer>
   </> ;

} 
