import { Handle, Position, getSmoothStepPath, BaseEdge, EdgeLabelRenderer, MarkerType, type Edge, type EdgeProps } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { useState} from 'react';
export default function ParentEdge({ id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  markerEnd
 }: EdgeProps<Edge>){
  //const [displayNumber, setDisplayNumber] = useState(data.number);
    const [edgePath, labelX, labelY] = getSmoothStepPath({sourceX, sourceY, sourcePosition, targetX, targetY, targetPosition});
    //TODO color and label is editable
   return <>
    <BaseEdge id={id} path={edgePath} style={{ stroke: 'red' }} markerEnd={markerEnd}/>
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
