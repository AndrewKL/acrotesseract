$( document ).ready(function() {
    console.log("setting up graph");

    $.ajax({
        dataType: "json",
        url: '/graph/data'
    }).then(function(data){
        console.log(data);
        var transformedData = data.poses.map(function(pose){
            return { // node a
                data: {
                    id: pose.pose_id,
                    name: pose.name
                },
                group: 'nodes',
                classes: 'acro-pose'
            }
        }).concat(data.transitions.map(function(trans){
            return { // edge ab
                data: {
                    id: 'transition_'+trans.transition_id,
                    source: trans.pose_from,
                    target: trans.pose_to
                },
                group: 'edges'
            }
        }));

        console.log(transformedData);

        var layout = {
            name:'cola',
            fit:false, //don't move the view port on reflow
            edgeLength: 175 // This is the length that cola tries to reach for all edges
        };

        window.acroLayout = layout;

        var cy = cytoscape({

            container: document.getElementById('acro-graph'), // container to render in

            elements: transformedData,

            style: [ // the stylesheet for the graph
                {
                    selector: 'node',
                    style: {
                        'background-color': '#666',
                        'label': 'data(name)'
                    }
                },

                {
                    selector: 'edge',
                    style: {
                        'width': 3,
                        'line-color': '#ccc',
                        'target-arrow-color': '#ccc',
                        'target-arrow-shape': 'triangle'
                    }
                }
            ],

            layout: layout,
            wheelSensitivity: 0.1 //cytoscape was a bit twitchy when zooming in and out
        });

        console.log(cy)

        //reflow when an element is dragged around
        cy.on("drag",function(){
            cy.makeLayout(layout).run()
        })

        //clicked on a node
        cy.on('tap', 'node', function(evt){
            var node = evt.target;
            console.log( 'tapped ' + node.id() );
            console.log(node);
            window.location = '/poses/'+node.id();
        });


        window.acroGraph = cy;
    })
});

