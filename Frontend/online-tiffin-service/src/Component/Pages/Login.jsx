import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { loadCaptchaEnginge, validateCaptcha, LoadCanvasTemplate } from "react-simple-captcha";
import swal from "sweetalert";
import "../../App.css";
import { IP_ADDRS } from "../../Service/Constant";

function Login(props) {
    const [data, setData] = useState({
        username: "",
        password: "",
    });

    const [passType, setPassType] = useState("password");
    const [isChecked, setIsChecked] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        loadCaptchaEnginge(6, 'red', 'black', 'upper');
    }, []);

    const handleShowPassword = () => {
        setIsChecked(!isChecked);
    };

    useEffect(() => {
        setPassType(isChecked ? "text" : "password");
    }, [isChecked]);

    const changeHandler = (e) => {
        setData((prevData) => ({
            ...prevData,
            [e.target.name]: e.target.value
        }));
    };

    const submitData = async (e) => {
        e.preventDefault();

        if (!data.username || !data.password) {
            swal("Error", "Username and Password cannot be empty!", "error");
            return;
        }

        let userCaptcha = document.getElementById('user_captcha_input').value;
        if (!validateCaptcha(userCaptcha)) {
            swal("Captcha Error", "Incorrect Captcha!", "error");
            return;
        }

        try {
            const response = await axios.post(`${IP_ADDRS}/auth/signin`, {
                email: data.username,
                password: data.password
            });

            console.log("Login Response:", response.data); // Debug Log

            if (!response.data || !response.data.role) {
                swal("Error", "Invalid response from server!", "error");
                return;
            }

            props.isLogged(true);

            if (response.data.role.includes("ROLE_CUSTOMER")) {
                sessionStorage.setItem("customer", JSON.stringify(response.data));
                navigate(`/customer`);
            } else if (response.data.role.includes("ROLE_VENDOR")) {
                sessionStorage.setItem("vendor", JSON.stringify(response.data));
                console.log("Vendor Data Saved:", sessionStorage.getItem("vendor")); // Debug Log
                navigate(`/vendor`);
            } else if (response.data.role.includes("ROLE_ADMIN")) {
                sessionStorage.setItem("admin", JSON.stringify(response.data));
                navigate(`/admin`);
            } else {
                swal("Error", "Unknown user role!", "error");
            }
        } catch (err) {
            console.error("Login Error:", err.response ? err.response.data : err.message);
            swal("Login Failed", "Invalid email or password!", "error");
        }
    };

    return (
        <div>
            <br /><br />
            <div className="container">
                <div className="row">
                    <div className="card col-md-6 offset-md-3">
                        <h2 className="text-center"><b>Login</b></h2>
                        <hr />

                        <form style={{ textAlign: "center" }} onSubmit={submitData}>
                            <div className="form-group">
                                <label>Email Id:</label>
                                <input
                                    type="email"
                                    placeholder="Enter Email ID"
                                    name="username"
                                    className="form-control"
                                    value={data.username}
                                    onChange={changeHandler}
                                    style={{ width: 300, margin: "auto" }}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Password:</label>
                                <input
                                    type={passType}
                                    placeholder="Password"
                                    name="password"
                                    className="form-control"
                                    value={data.password}
                                    onChange={changeHandler}
                                    style={{ width: 300, margin: "auto" }}
                                    required
                                />
                                <span>
                                    <input type="checkbox" checked={isChecked} onChange={handleShowPassword} id="show" />
                                    &emsp;<label htmlFor="show">Show Password</label>
                                </span>
                            </div>

                            <div className="form-group" style={{ marginTop: "20px", textAlign: "center" }}>
                                <LoadCanvasTemplate />
                            </div>

                            <div className="form-group" style={{ textAlign: "center" }}>
                                <label>Enter Captcha:</label>
                                <input
                                    type="text"
                                    placeholder="Enter Captcha"
                                    id="user_captcha_input"
                                    className="form-control"
                                    style={{ width: 200, margin: "auto" }}
                                    required
                                />
                            </div>

                            <div>
                                <table style={{ margin: "auto" }}>
                                    <tbody>
                                        <tr>
                                            <td><button type="submit" className="btn btn-success">Login</button></td>
                                            <td><button type="button" className="btn btn-danger" onClick={() => navigate("/")}>Cancel</button></td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </form>

                        <div style={{ textAlign: "center" }}>
                            <a href="/forgotpassword">Forgot password? Click here...</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Login;
