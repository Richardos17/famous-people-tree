/*import { Person } from "@/types/Person";
import { Node } from "@xyflow/react";
import { HandlePosition } from "@/app/types/HandlePosition";
import { calculateChildPosition, calculateMarriagePosition, calculateParentPosition } from "./positionCalculator";

export function updateHandlePosition(
  nodeId: string,
  options: {
    top?: boolean;
    bottom?: boolean;
    left?: boolean;
    right?: boolean;
  }
) {
  setNodes((nodes) =>
    nodes.map((node) => {
      if (node.id !== nodeId) {
        return node;
      }
      const handlePosition:HandlePosition = node.data.handlePosition
      const {top, bottom, left, right } = options
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

      return {
        ...node,
        data: {
          ...node.data,
          handlePosition: handlePosition,
        },
      };
    })
  );
}
  
export function addParent(newPerson:Person, sourcePersonNode:Node):Node{
    const newNode: Node = {
      id: newPerson.wikidataId,
      data: {
        person: newPerson,
        handlePosition: {
          bottom: true,
          top:false,
          right: false,
          left: false
        }
      },
      position: calculateParentPosition(sourcePersonNode.position, sourcePersonNode.id),
      type: "person"
    };
    setNodes((oldNodes) => [...oldNodes, newNode]);
    
    updateHandlePosition(sourcePersonNode.id, {top: true});
    setEdges((oldEdges) =>{
      return [...oldEdges,  {data:{edgeType: EdgeType.Parent}, id: sourcePersonNode.id+'-'+newPerson.wikidataId, source: sourcePersonNode.id, sourceHandle: 't'+sourcePersonNode.id, target: newPerson.wikidataId, targetHandle: 'b'+newPerson.wikidataId, type: 'parent', markerEnd: { type: MarkerType.ArrowClosed }  }]})
    personEdges.current.set(newNode.id, {
      top: 0,
      bottom: 1,
      left: 0,
      right: 0,
    });
    const person = personEdges.current.get(sourcePersonNode.id);

    if (!person) 
      throw new Error("Node does not exist");
    personEdges.current.set(sourcePersonNode.id, {
      ...person,
      top: person.top + 1,
    });
    return newNode;

  }
export function addChild(newPerson:Person, sourcePersonNode:Node){
    const newNode: Node = {
      id: newPerson.wikidataId,
      data: {
        person: newPerson,
        handlePosition: {
          bottom: false,
          top:true,
          right: false,
          left: false
        }
      },
      position: calculateChildPosition(sourcePersonNode.position, sourcePersonNode.id),
      type: "person"
    };
    setNodes((oldNodes) => [...oldNodes, newNode]);
    
    updateHandlePosition(sourcePersonNode.id, {bottom: true});
    setEdges((oldEdges) =>{
      return [...oldEdges,  {data:{edgeType: EdgeType.Child}, id: sourcePersonNode.id+'-'+newPerson.wikidataId, source: sourcePersonNode.id, sourceHandle: 'b'+sourcePersonNode.id, target: newPerson.wikidataId, targetHandle: 't'+newPerson.wikidataId, type: 'parent', markerEnd: { type: MarkerType.ArrowClosed }}]})
    personEdges.current.set(newNode.id, {
      top: 1,
      bottom: 0,
      left: 0,
      right: 0,
    });
    const person = personEdges.current.get(sourcePersonNode.id);

    if (!person) 
      throw new Error("Node does not exist");
    personEdges.current.set(sourcePersonNode.id, {
      ...person,
      bottom: person.bottom + 1,
    });
    return newNode;
  }
export function addMarriage(newPerson:Person, sourcePersonNode:Node){
    const newNode: Node = {
      id: newPerson.wikidataId,
      data: {
        person: newPerson,
        handlePosition: {
          bottom: false,
          top:false,
          right: false,
          left: true
        }
      },
      position: calculateMarriagePosition(sourcePersonNode.position, sourcePersonNode.id),
      type: "person"
    };
    setNodes((oldNodes) => [...oldNodes, newNode]);
    
    updateHandlePosition(sourcePersonNode.id, {right: true});
    setEdges((oldEdges) =>{
      return [...oldEdges,  {data:{edgeType: EdgeType.Spouse, marriageStartDate: new Date(2013, 1, 4), marriageEndDate: new Date(2017, 4, 7)}, id: sourcePersonNode.id+'-'+newPerson.wikidataId, source: sourcePersonNode.id, sourceHandle: 'r'+sourcePersonNode.id, target: newPerson.wikidataId, targetHandle: 'l'+newPerson.wikidataId, type: 'parent', markerEnd: { type: MarkerType.ArrowClosed }}]})
    personEdges.current.set(newNode.id, {
      top: 0,
      bottom: 0,
      left: 1,
      right: 0,
    });
    const person = personEdges.current.get(sourcePersonNode.id);

    if (!person) 
      throw new Error("Node does not exist");
    personEdges.current.set(sourcePersonNode.id, {
      ...person,
      right: person.right + 1,
    });
    return newNode;
  }
export function addRootNode(newPerson:Person):Node{
    const newNode: Node = {
      id: newPerson.wikidataId,
      data: {
        person: newPerson,
        handlePosition: {
          top:false,
          bottom:false,
          right:false,
          left:false
        }
      },
      position: { x: 10, y: 500 },
      type: "person"
    };

    setNodes((oldNodes) => [...oldNodes, newNode]);
    personEdges.current.set(newNode.id, {
    top: 0,
    bottom: 0,
    left: 0,
    right: 0,
  });

    return newNode;
  }*/