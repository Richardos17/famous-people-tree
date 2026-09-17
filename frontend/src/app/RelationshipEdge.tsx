import { Handle, Position, getSmoothStepPath, BaseEdge, EdgeLabelRenderer, MarkerType, type Edge, type EdgeProps } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { useState} from 'react';
import { EdgeType } from './Enums';
type RelationshipEdgeType = Edge<{ edgeType: EdgeType, marriageStartDate: Date, marriageEndDate: Date }, 'relationship'>;
const strokeColor:Record<EdgeType, string> = {
    [EdgeType.Parent]: "red",
    [EdgeType.Child]: "green",
    [EdgeType.Spouse]: "yellow",
}
const edgeLabel:Record<EdgeType, string> = {
    [EdgeType.Parent]: "Parent",
    [EdgeType.Child]: "Child",
    [EdgeType.Spouse]: "Spouse",
}
export default function RelationshipEdge( { data, id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  markerEnd,
  
 }: EdgeProps<RelationshipEdgeType>){
  //const [displayNumber, setDisplayNumber] = useState(data.number);
    const [edgePath, labelX, labelY] = getSmoothStepPath({sourceX, sourceY, sourcePosition, targetX, targetY, targetPosition});
    //TODO make marriage properly with existing checks and styling
   
   return <>
    <BaseEdge id={id} path={edgePath} style={{ stroke: strokeColor[data?.edgeType ?? EdgeType.Parent] }} markerEnd={markerEnd}/>
    <EdgeLabelRenderer>
            <div
            style={{
                position: 'absolute',
                transform: `translate(-50%, -50%) translate(${labelX}px, ${labelY}px)`,
            }}
            >
             {edgeLabel[data?.edgeType ?? EdgeType.Parent]}<br/>
             {data?.edgeType == EdgeType.Spouse ? 
             "Marriage start: " + data.marriageStartDate.toLocaleDateString(): ""}<br/>
             {data?.edgeType == EdgeType.Spouse ? 
             "\nMarriage end: " + data.marriageStartDate.toLocaleDateString(): ""}
            </div>
        </EdgeLabelRenderer>
   </> ;

} 
