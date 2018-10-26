$( document ).ready(function() {
    console.log("setting up modals");
    var source   = document.getElementById("pose-search-template").innerHTML;
    var template = Handlebars.compile(source);

    var searchModalSetup = function($modal,$input,$openSearch){
        $openSearch.click(function(){
            $modal.show()
        });

        $modal.find(".close").click(function(){
            $modal.hide()
        });

        $modal.find(".search-text").on('keyup paste', $.throttle( 250, function(){
            console.log("searching poses");
            var searchText = $modal.find('.search-text').val();
            $.ajax({
                dataType: "json",
                url: '/search',
                data: {'searchText':searchText},
                success: function(data){
                    var html = template(data);
                    $modal.find(".pose-search-results")[0].innerHTML = html;
                    $modal.find(".select-pose-btn").click(function(e){
                        var poseId = $(e.currentTarget).attr("x-pose-id");
                        $input.val(poseId);
                        $modal.hide();
                    })
                }
            });
        }));
    };

    $fromPoseModal = $('#search-from-poses-modal');
    $fromSearchBtn = $('#search-from-pose');
    $fromPoseInput = $('#pose-from-id');

    searchModalSetup($fromPoseModal,$fromPoseInput,$fromSearchBtn);

    $toPoseModal = $('#search-to-poses-modal');
    $toSearchBtn = $('#search-to-pose');
    $toPoseInput = $('#pose-to-id');

    searchModalSetup($toPoseModal,$toPoseInput,$toSearchBtn)

});

