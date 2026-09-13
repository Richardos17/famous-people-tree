"use client"
import '@xyflow/react/dist/style.css';
import { ReactFlow, Background, Controls, addEdge, applyNodeChanges, applyEdgeChanges ,type Node, type Edge, type OnConnect, type OnNodesChange, type OnEdgesChange } from '@xyflow/react';
import {useState, useCallback} from 'react';
import PersonNode from './PersonNode';
import HasParentEdge from './HasParentEdge';
import { VerticalHandlePosition, HorizontalHandlePosition } from './PersonNode';
const nodeTypes = {
  person: PersonNode,
};
const edgeTypes = {
  parent: HasParentEdge,
};
export default function Home(){
  
  const [nodes, setNodes]= useState<Node[]>([
    {id:"1", data : {name: "Richard 2", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string", handlePosition: {verticalHandlePosition: VerticalHandlePosition.Bottom, horizontalHandlePosition: HorizontalHandlePosition.None}}, position: {x: 10, y:0}, type: "person"},
    {id:"2", data : {name: "Richard grgrgrgrgrgrgrg", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string", handlePosition: {verticalHandlePosition: VerticalHandlePosition.Top, horizontalHandlePosition: HorizontalHandlePosition.None}}, position: {x: 10, y:500}, type: "person"}
  ])
  const [edges, setEdges] = useState<Edge[]>([{
    id: 'n1-n2',
    source: '1',
    target: '2',
    type: 'parent'
  },]);
  const onConnect: OnConnect = useCallback((connection) => {
    setEdges((oldEdges) => addEdge<Edge>({...connection, type:"parent"}, oldEdges))
  }, [])
    const onNodesChange: OnNodesChange = useCallback((connection) => setNodes((oldEdges) => applyNodeChanges<Node>(connection, oldEdges)), [])
    const onEdgesChange: OnEdgesChange = useCallback((connection) => {setEdges((oldEdges) => applyEdgeChanges<Edge>(connection, oldEdges))}, [])

    return <div style={{width: 1000, height: 500}}>
      <ReactFlow nodes={nodes} onConnect={onConnect} connectionMode="loose" edges={edges} onNodesChange={onNodesChange} onEdgesChange={onEdgesChange} nodeTypes={nodeTypes} edgeTypes={edgeTypes}>
        <Controls/>
        <Background/>
      </ReactFlow>
      
    </div>
}