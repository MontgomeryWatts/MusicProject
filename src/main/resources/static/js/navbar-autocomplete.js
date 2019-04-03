$('#artist_input').autocomplete({
    minLength: 3,
    source: function (request, response) {
        $.ajax(
            {
                type: "POST",
                contentType: "application/json",
                url: "/autocomplete",
                dataType: "json",
                data: {
                    q: request.term
                },
                success: function (data){
                    // This mapping assumes Mongo is being used,
                    // Should be changed in the future.
                    response(data.map(artist => artist.name));
                }
            }
        );
    }
});