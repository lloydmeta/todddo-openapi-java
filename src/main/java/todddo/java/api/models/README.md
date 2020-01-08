## Models

All of the annotations (Jackson JSON related ones, Introspection) are needed in order to guarantee that JSON serdes
works properly in a Graal Native image. This also includes the manual `*Builder` stuff.

If any of these are missing, you'll likely see runtime failures.
