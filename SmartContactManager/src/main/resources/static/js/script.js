console.log("This is script file");

// how to open this file in VSCODE
//script.js => showIn => System Exploser => (open file with VSCODE)

const toggleSidebar = () => {

	if ($(".sidebar").is(":visible")) {
		//true
		//hide sidebar
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%")

	} else {
		//false
		//show sidebar
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%")
	}

};


/*
//search box

const search = () => {
	//console.log("searching...");

	let query = $("#search-input").val();


	if (query == "") {
		$(".search-result").hide();

	} else {
		//search
		console.log(query);

		//sending request to server

		let url = `http://localhost:8181/search/${query}`;

		fetch(url).then((Response) => {
			return Response.json();
		})
			.then((data) => {
				//data
				// console.log(data);

				let text = `<div class='list-group'>`;

				data.forEach((contact) => {
					text += `<a href="#" class='list-group-item list-group-action'> ${contact.name} </a>`
				});

				text += `</div>`;

				$(".search-result").html(text);

				$(".search-result").show();

			});



	}


};
*/