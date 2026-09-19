import { PersonConnectedEdges } from "@/app/types/PersonConnectedEdges";
import { FamilyGraph } from "@/types/FamilyGraph";
import { FamilyTreeLayout } from "@/types/FamilyTreeLayout";
import { Person } from "@/types/Person";
import { type Node, type Edge, MarkerType } from '@xyflow/react';
import { calculateParentPosition, calculateChildPosition } from "./positionCalculator";
import { HandlePosition } from "@/app/types/HandlePosition";
import { EdgeType } from "./edgeType";
import RelationshipEdge from "@/components/RelationshipEdge";

export function layoutFamilyTree(
  graph?: FamilyGraph,
  centerNodeId?: string,
): FamilyTreeLayout {
   const personEdges: Map<string, PersonConnectedEdges> = new Map();
  const nodes: Map<string, Node> = new Map();
  const edges: Edge[] = [];
  //create layout with nodes
  if(graph === undefined)
    throw new Error("Graph is undefined");
  if(centerNodeId === undefined)
    throw new Error("CenterNodeId is undefined");
  const centerPerson: Person | undefined = graph.persons.get(centerNodeId);
  if(centerPerson === undefined)
    throw new Error("Center person id not found in graph.");
  nodes.set(centerPerson.wikidataId, {
    id: centerPerson.wikidataId,
    data: {
      person: centerPerson,
      handlePosition: {
        top: false,
        bottom: false,
        right: false,
        left: false,
      },
    },
    position: { x: 0, y: 0 },
    type: "person",
  });
  personEdges.set(centerPerson.wikidataId, {
    top: 0,
    bottom: 0,
    left: 0,
    right: 0,
  });
  let queue: string[] = [centerNodeId];
  let visited = new Set<string>([centerNodeId]);
  while (queue.length > 0) {
    const current: string | undefined = queue.shift();
   // if(visited.has(current!))
   //   continue
    visited.add(current!)
    graph.relationships
      .filter((relationship) =>( relationship.fromWikidataId === current)&& relationship.type === EdgeType.Parent)
      .forEach((relationship) => {
        const newPerson: Person | undefined = graph.persons.get(relationship.toWikidataId);
        if(newPerson === undefined)
          throw new Error("Person connected by edge is missing.")
        nodes.set(newPerson.wikidataId, {
          id: newPerson.wikidataId,
          data: {
            person: newPerson,
            handlePosition: {
              top: false,
              bottom: true,
              right: false,
              left: false,
            },
          },
          position: calculateParentPosition(nodes.get(relationship.fromWikidataId)?.position, personEdges.get(relationship.fromWikidataId)),
          type: "person",
        });
        updateHandlePosition(nodes.get(relationship.fromWikidataId), {top:true})
        edges.push({data:{edgeType: EdgeType.Parent}, id: relationship.fromWikidataId+'-'+newPerson.wikidataId, source: relationship.fromWikidataId, sourceHandle: 't'+relationship.fromWikidataId, target: newPerson.wikidataId, targetHandle: 'b'+newPerson.wikidataId, type: 'parent', markerEnd: { type: MarkerType.ArrowClosed }})
        personEdges.set(newPerson.wikidataId, {
          top: 0,
          bottom: 1,
          left: 0,
          right: 0,
        });
        const sourcePersonEdges = personEdges.get(relationship.fromWikidataId);
        if (!sourcePersonEdges) 
          throw new Error("Node does not exist");
        sourcePersonEdges.top += 1
        if(!visited.has(relationship.toWikidataId))
          queue.push(relationship.toWikidataId)
      });      
  }
  queue = [centerNodeId];
  visited = new Set<string>([centerNodeId]);
  while (queue.length > 0) {
    const current: string | undefined = queue.shift();
    visited.add(current!)
    
      graph.relationships
      .filter((relationship) =>( relationship.toWikidataId === current)&& relationship.type === EdgeType.Parent)
      .forEach((relationship) => {

        const newPerson: Person | undefined = graph.persons.get(relationship.fromWikidataId);
        if(newPerson === undefined)
          throw new Error("Person connected by edge is missing.")
        nodes.set(newPerson.wikidataId, {
          id: newPerson.wikidataId,
          data: {
            person: newPerson,
            handlePosition: {
              top: true,
              bottom: false,
              right: false,
              left: false,
            },
          },
          position: calculateChildPosition(nodes.get(relationship.toWikidataId)?.position, personEdges.get(relationship.toWikidataId)),
          type: "person",
        });
        updateHandlePosition(nodes.get(relationship.toWikidataId), {bottom:true})
        edges.push({data:{edgeType: EdgeType.Child}, id: relationship.toWikidataId+'-'+newPerson.wikidataId, source: relationship.toWikidataId, sourceHandle: 'b'+relationship.toWikidataId, target: newPerson.wikidataId, targetHandle: 't'+newPerson.wikidataId, type: 'parent', markerEnd: { type: MarkerType.ArrowClosed }})
        personEdges.set(newPerson.wikidataId, {
          top: 1,
          bottom: 0,
          left: 0,
          right: 0,
        });
        const sourcePersonEdges = personEdges.get(relationship.toWikidataId);
        if (!sourcePersonEdges) 
          throw new Error("Node does not exist");
        sourcePersonEdges.bottom += 1
        if(!visited.has(relationship.fromWikidataId))
          queue.push(relationship.fromWikidataId)
      });
  }
  console.log(nodes, edges)
  return {nodes: nodes.values().toArray(), edges: edges}
}
function updateHandlePosition(//TODO maybe doesnt work, use the old way
  node: Node | undefined,
  options: {
    top?: boolean;
    bottom?: boolean;
    left?: boolean;
    right?: boolean;
  }
) {

      if(node === undefined)
        throw new Error("Selected node is undefined");
      const {top, bottom, left, right } = options
      const handlePosition:HandlePosition = node.data.handlePosition
      
      if(top != undefined){
        handlePosition.top = top;
      }
      if(bottom != undefined){
        handlePosition.bottom = bottom;
      }
      if(right != undefined){
        handlePosition.right = right;
      }
      if(left != undefined){
        handlePosition.left = left;
      }
}