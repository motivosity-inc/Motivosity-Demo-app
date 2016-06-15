(function (app) {

	app.AppComponent = ng.core.Component({
			selector: 'demo-app',
			templateUrl: '../../demo-app-component.html'})
		.Class({
			constructor: function () {
				this.userList = [];

				this.getUserList = function () {
					var request = new XMLHttpRequest();
					request.open("GET", "/api/motivosity/userlist", false);
					request.send(null);
					this.userList = JSON.parse(request.responseText);
				}
			}
		});

})(window.app || (window.app = {}));