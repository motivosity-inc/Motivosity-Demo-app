(function (app) {

	app.AppComponent = ng.core.Component({
			selector: 'demo-app',
			templateUrl: '../../demo-app-component.html'})
		.Class({
			constructor: function () {
				var component = this;
				component.userList = [];

				this.getUserList = function () {
					var request = new XMLHttpRequest();
					request.open("GET", "/api/motivosity/userlist", true);
					request.send();

					request.onreadystatechange = function() {
						if(request.readyState == XMLHttpRequest.DONE && request.status == 200) {
							component.userList = JSON.parse(request.responseText);
						}
					}
				}
				
				this.refreshToken = function () {
					var request = new XMLHttpRequest();
					request.open("GET", "/api/refreshToken", true);
					request.onreadystatechange = function() {
						if(request.readyState == XMLHttpRequest.DONE && request.status == 200) {
							component.userList = [];
						}
					}
					request.send();
				}
			}
		});

})(window.app || (window.app = {}));