import { getSmoothStepPath, BaseEdge, EdgeLabelRenderer, type Edge, type EdgeProps } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { EdgeType } from '@/graph/edgeType';
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
    const [edgePath, labelX, labelY] = getSmoothStepPath({sourceX, sourceY, sourcePosition, targetX, targetY, targetPosition});
   return <>
    <BaseEdge id={id} path={edgePath} style={{ stroke: strokeColor[data?.edgeType ?? EdgeType.Parent] }} markerEnd={markerEnd}/>
    <EdgeLabelRenderer>
            <div
            style={{
                position: 'absolute',
                transform: data?.edgeType === EdgeType.Parent ? `translate(-50%, -50%) translate(${labelX}px, ${labelY}px)` : `translate(-50%, -50%) translate(${targetX - 120}px, ${labelY}px)`,//Compute this better
            }}
            >
             {edgeLabel[data?.edgeType ?? EdgeType.Parent]}<br/>
             {data?.edgeType === EdgeType.Spouse && data.marriageStartDate ? 
             "Marriage start: " + data.marriageStartDate.toLocaleDateString(): ""}<br/>
             {data?.edgeType === EdgeType.Spouse && data.marriageEndDate ? 
             "\nMarriage end: " + data.marriageEndDate.toLocaleDateString(): ""}
            </div>
        </EdgeLabelRenderer>
   </> ;

} 
