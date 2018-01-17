(function (app) {

	var serverComponent = ng.core.Component({
		selector: 'server-app',
		templateUrl: '../../server-app-component.html'
	})
		.Class({
			constructor: function () {
				var component = this;
				component.userList = [];

				this.getUserList = function () {
					var request = new XMLHttpRequest();
					request.open("GET", "/api/motivosity/userlist", true);
					request.send();

					request.onreadystatechange = function () {
						if (request.readyState == XMLHttpRequest.DONE && request.status == 200) {
							component.userList = JSON.parse(request.responseText);
						}
					}
				}

				this.refreshToken = function () {
					var request = new XMLHttpRequest();
					request.open("GET", "/api/refreshToken", true);
					request.onreadystatechange = function () {
						if (request.readyState == XMLHttpRequest.DONE && request.status == 200) {
							component.userList = [];
						}
					}
					request.send();
				}
			}
		});

	var jsComponent = ng.core.Component({
		selector: 'js-app',
		templateUrl: '../../js-app-component.html'
	})
		.Class({
			constructor: function () {
				var component = this;
				// this.url = "https://app.motivosity-sandbox.com"
				this.url = "http://localhost:8080/motivosity";
				this.loginData = {};
				this.loginData.username = "leslie@parks.com";
				this.loginData.password = "test";
				this.loginData.appId = "";
				this.accessToken = null;
				this.refreshToken = null;

				this.signin = function () {
					var request = new XMLHttpRequest();
					request.open("POST", this.url + "/auth/v1/signin", true);
					request.setRequestHeader("Content-Type", "application/json");
					request.onreadystatechange = function () {
						if (request.readyState == XMLHttpRequest.DONE && request.status == 200) {
							var response = JSON.parse(request.responseText);
							component.accessToken = response.response.accessToken;
							component.refreshToken = response.response.refreshToken;
						}
					}

					request.send(JSON.stringify(this.loginData));
				}

				this.doRefreshToken = function () {
					var refreshTokenData = {
						appId: this.loginData.appId,
						refreshToken: this.refreshToken
					};

					var request = new XMLHttpRequest();
					request.open("POST", this.url + "/auth/v1/refresh", true);
					request.setRequestHeader("Content-Type", "application/json");
					request.onreadystatechange = function () {
						if (request.readyState == XMLHttpRequest.DONE && request.status == 200) {
							var response = JSON.parse(request.responseText);
							component.accessToken = response.response.accessToken;
							component.refreshToken = response.response.refreshToken;
						}
					}

					request.send(JSON.stringify(refreshTokenData));
				}

				this.getUserList = function () {
					var request = new XMLHttpRequest();
					request.open("GET", this.url + "/api/v2/user", true);
					request.setRequestHeader("Authorization", "Bearer " + this.accessToken);
					request.send();

					request.onreadystatechange = function () {
						if (request.readyState == XMLHttpRequest.DONE && request.status == 200) {
							component.userList = JSON.parse(request.responseText).response;
						}
					}
				}
			}
		});

	app.AppComponent =
		ng.core.Component({
			selector: 'demo',
			templateUrl: '../../demo-component.html',
			directives: [serverComponent, jsComponent]
		})
			.Class({
				constructor: function () {
				}
			});

})(window.app || (window.app = {}));