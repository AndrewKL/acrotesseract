$( document ).ready(function() {
    var search = function() {
        $.ajax({
            dataType: "json",
            url: '/search',
            data: {'searchText':$('#search-text').val()},
            success: function(data){
                var html = template(data)
                document.getElementById('search-results').innerHTML = html
            }
        });
    }
    var source   = document.getElementById("search-template").innerHTML;
    var template = Handlebars.compile(source);
    $('#search-text').on('keyup paste', $.throttle( 250, search));

    search();
});

