"use client"
import '@xyflow/react/dist/style.css';
import { ReactFlow, Background, Controls, addEdge, applyNodeChanges, applyEdgeChanges , MarkerType,type Node, type Edge, type OnConnect, type OnNodesChange, type OnEdgesChange, ConnectionMode, XYPosition } from '@xyflow/react';
import {useState, useCallback, useEffect, useRef} from 'react';
import PersonNode from './PersonNode';
import RelationshipEdge from './RelationshipEdge';
import { Person } from './types/Person';
import { PersonConnectedEdges } from './types/PersonConnectedEdges';
import {type HandlePosition } from './types/HandlePosition';
import { EdgeType } from './Enums';
const nodeTypes = {
  person: PersonNode,
};
const edgeTypes = {
  parent: RelationshipEdge,
};
export default function Home(){
  
  const [nodes, setNodes]= useState<Node[]>([])
  const [edges, setEdges] = useState<Edge[]>([]);
  const personEdges = useRef<Map<string, PersonConnectedEdges>>(new Map());

 function updateHandlePosition(
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
  function calculateParentPosition(rootNodePosition:XYPosition, rootNodeId:string):XYPosition{
    const personEdgeNumber = personEdges.current.get(rootNodeId);

    if (personEdgeNumber === undefined) {
      throw new Error("Root node is undefined");
    }
    
    let xPosition:number = 0;
    if(personEdgeNumber.top % 2 == 0)
      xPosition = rootNodePosition.x + 100 * (personEdgeNumber.top+1);
    else
      xPosition = rootNodePosition.x - 100 * (personEdgeNumber.top);
    return {x: xPosition , y: rootNodePosition.y -300};
  }
  function calculateChildPosition(rootNodePosition:XYPosition, rootNodeId:string):XYPosition{
    const personEdgeNumber = personEdges.current.get(rootNodeId);

    if (personEdgeNumber === undefined) {
      throw new Error("Root node is undefined");
    }
    
    let xPosition:number = 0;
    if(personEdgeNumber.top % 2 == 0)
      xPosition = rootNodePosition.x + 100 * (personEdgeNumber.top+1);
    else
      xPosition = rootNodePosition.x - 100 * (personEdgeNumber.top);
    return {x: xPosition , y: rootNodePosition.y + 300};
  }
  function addParent(newPerson:Person, sourcePersonNode:Node):Node{
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
      bottom: 0,
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
  function addChild(newPerson:Person, sourcePersonNode:Node){
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
      top: 0,
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
  function addRootNode(newPerson:Person):Node{
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
  }
      
  const onConnect: OnConnect = useCallback((connection) => {
    setEdges((oldEdges) => addEdge<Edge>({...connection, type:"parent"}, oldEdges))
  }, [])
    const onNodesChange: OnNodesChange = useCallback((connection) => setNodes((oldEdges) => applyNodeChanges<Node>(connection, oldEdges)), [])
    const onEdgesChange: OnEdgesChange = useCallback((connection) => {setEdges((oldEdges) => applyEdgeChanges<Edge>(connection, oldEdges))}, [])

    return <div style={{width: 1000, height: 500}}>
      <ReactFlow nodes={nodes} onConnect={onConnect} connectionMode={ConnectionMode.Loose} edges={edges} onNodesChange={onNodesChange} onEdgesChange={onEdgesChange} nodeTypes={nodeTypes} edgeTypes={edgeTypes}>
        <Controls/>
        <Background/>
      </ReactFlow>
      <button
  onClick={() => {
    const rootNode:Node = addRootNode({wikidataId: "Q19",name: "Richard 1", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"})

      const parent:Node = addParent({wikidataId: "Q9",name: "Richard 2", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"}, rootNode)
           addParent({wikidataId: "Q11",name: "Richard 2", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"}, rootNode)
           addParent({wikidataId: "Q141",name: "Richard 2", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"}, parent)
           addParent({wikidataId: "Q1441",name: "Richard 2", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"}, rootNode)
           addChild({wikidataId: "Q14441",name: "Richard 3", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"}, rootNode)

  }}
>
  Test
</button>
    </div>
}